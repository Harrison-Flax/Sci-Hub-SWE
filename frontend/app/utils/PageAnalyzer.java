package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class PageAnalyzer {
    private static final String STR_OUTPUT_FILE_NAME = "[ByPageAnalyzer]edges";

    private String controllersDir;
    private String servicesDir;
    private String viewsDir;
    private String routesPath;

    public String[] findEdges(String controllersDir, String servicesDir, String viewsDir, String routesPath) {
        this.controllersDir = controllersDir;
        this.servicesDir = servicesDir;
        this.viewsDir = viewsDir;
        this.routesPath = routesPath;

        List<String> edges = new LinkedList<String>();

        APIBodyVisitor apiBodyVisitor = new APIBodyVisitor(edges);

        File[] controllers = new File(this.controllersDir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("Controller.java") || name.contains("Application.java");
            }
        });
        for(File controller : controllers) {
            try{
                bfsAPI(controller, edges, apiBodyVisitor);
            } catch (Exception e) {
                ;
            }
        }

        File[] services = new File(this.servicesDir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("Service.java");
            }
        });
        for(File service : services) {
            try{
                bfsAPI(service, edges, apiBodyVisitor);
            } catch (Exception e) {
                ;
            }
        }

        try {
            readRoutes();
        } catch (Exception e) {
            ;
        }

        File[] views = new File(this.viewsDir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("scala.html");
            }
        });
        for (File view : views) {
            try {
                readView(view, edges);
            } catch (Exception e) {
                ;
            }
        }

        String[] result = new String[edges.size()];
        for (int i = 0; i < result.length; i++) result[i] = edges.get(i);
        return result;
    }

    private void readView(File viewFile, List<String> edges) throws IOException {
        Document doc = Jsoup.parse(viewFile, "utf-8");

        Elements links = doc.getElementsByTag("a");
        Iterator<Element> elementItr = links.iterator();
        while(elementItr.hasNext()) {
            Element link = elementItr.next();
            String target = link.attr("href");
            if (target.contains("@routes.")) {
                target = target.replace("@routes.", "");
                if (target.indexOf("(") > -1) {
                    target = target.substring(0, target.indexOf("("));
                    System.out.println("Page node " + viewFile.getName() + " links to method node " + target);
                    edges.add(viewFile.getName() + "\t" + target);
                }
            }
        }

        Elements forms = doc.getElementsByTag("form");
        elementItr = forms.iterator();
        while(elementItr.hasNext()) {
            Element form = elementItr.next();
            String target = form.attr("action");
            if (target.contains("@routes.")) {
                target = target.replace("@routes.", "");
                if (target.indexOf("(") > -1) {
                    target = target.substring(0, target.indexOf("("));
                    System.out.println("Page node " + viewFile.getName() + " links to method node " + target);
                    edges.add(viewFile.getName() + "\t" + target);
                }
            }
        }
    }

    private void readRoutes() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.routesPath));
        String line;
        Map<String, String> routeMap = new HashMap<String, String>();
        while(null != (line = br.readLine())) {
            if (line.startsWith("#") || line.trim().length() < 1 || line.split("[ ]+").length < 3) continue;
            if (line.lastIndexOf("(") != -1)  line = line.substring(0, line.lastIndexOf("("));
            String[] content = line.split("[ ]+");
            routeMap.put(content[1], content[2].replace("controllers.", ""));
        }
        br.close();
//        System.out.println(routeMap.values());
    }

    private void bfsAPI(File apiFile, List<String> edges, APIBodyVisitor apiBodyVisitor) throws FileNotFoundException {
        CompilationUnit cu = JavaParser.parse(apiFile);
        NodeList<TypeDeclaration<?>> types = cu.getTypes();

        for (TypeDeclaration<?> type : types) {
            NodeList<BodyDeclaration<?>> bodies = type.getMembers();
            for(BodyDeclaration<?> body : bodies) {
                if (body.isMethodDeclaration() && ((MethodDeclaration)body).getType().asString().endsWith("Result")) {
                    NodeList<AnnotationExpr> annotations = body.getAnnotations();
                    boolean methodTraced = false;
                    for (AnnotationExpr annotation : annotations) {
                        if (annotation.toString().contains("OperationLoggingAction.class")) {
                            methodTraced = true;
                            break;
                        }
                    }
                    if (!methodTraced) continue;
                    apiBodyVisitor.visit(body.asMethodDeclaration(), type.getNameAsString() + "." + body.asMethodDeclaration().getNameAsString());
                }
            }
        }
    }

    private static class APIBodyVisitor extends VoidVisitorAdapter<String> {
        private List<String> edges;

        public APIBodyVisitor(List<String> edges) {
            this.edges = edges;
        }

        public void visit(ReturnStmt stmt, String args) {
            super.visit(stmt, args);
            Expression expression = stmt.getExpression().get();
            if (expression.isMethodCallExpr()) {
                MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
                String methodCallName = methodCallExpr.getNameAsString();
                if ("ok".equals(methodCallName) || "redirect".equals(methodCallName)) {
                    NodeList<Expression> argExprs = methodCallExpr.getArguments();
                    for(Expression argExp : argExprs) {
                        if (argExp.isMethodCallExpr() && ("render".equals(argExp.asMethodCallExpr().getNameAsString()))) {
                            System.out.println("Method node " + args + " links to page node " + argExp.asMethodCallExpr().getScope().get() + ".scala.html");
                            edges.add(args + "\t" + argExp.asMethodCallExpr().getScope().get() + ".scala.html");
                        }
                    }
                }
            }
        }
    }

    public static void main(String... args) throws IOException {
        assert args.length == 4;

        System.out.println("Input: directory paths for controllers, services, views and file path for routes.");
        String STR_DIR_CONTROLLERS = args[0];
        String STR_DIR_SERVICES = args[1];
        String STR_DIR_VIEWS = args[2];
        String STR_FILE_ROUTES = args[3];
        PageAnalyzer pa = new PageAnalyzer();
        String[] edges = pa.findEdges(STR_DIR_CONTROLLERS, STR_DIR_SERVICES, STR_DIR_VIEWS, STR_FILE_ROUTES);
        FileWriter fileWriter = new FileWriter(STR_OUTPUT_FILE_NAME + "_" + System.currentTimeMillis() + ".txt");
        for(String edge: edges) fileWriter.write(edge + "\n");
    }
}

