import java.io.BufferedWriter;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class ExtractTopTerms{
        public static void main(String args[]) throws IOException {
            List<String> list = new ArrayList<String>();
            List<String> input = new ArrayList<String>();
            List<String> output = new ArrayList<String>();
            List<String> outputTerms = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(new File("out.csv-document-topic-distributuions.csv")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            reader = new BufferedReader(new FileReader(new File("out.csv-top-terms.csv")));
            line = reader.readLine();
            System.out.println("***"+line);
            while ((line = reader.readLine()) != null) {
                System.out.println("***");
                outputTerms.add(line);
            }
            File extractedFile = new File("ExtractedByTermsFile.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(extractedFile));
            
            
            int skipLineZero=0;
            for (String line2 : list) {
                if(skipLineZero>0){
                String[] res1 = line2.split("\",");
                input.add(res1[0]);
                output.add(res1[1]);
                }
                    skipLineZero++;
                
            }
            int inputIndex = 0;
            for (String o : output) {
                String[] res2 = o.split(",");
                float maxFloat = -Float.MIN_VALUE;
                int count = 0;
                int index = 0;
                for (String r : res2) {
                    float f = Float.parseFloat(r);
                    if (f > maxFloat) {
                        maxFloat = f;
                        index = count;
                    }
                    count++;
                }
                
                System.out.println(input.get(inputIndex) + "," + maxFloat+","+outputTerms.get(index) + "\n");
                
                writer.write(input.get(inputIndex) + "," + outputTerms.get(index) + "\n");
                inputIndex++;
            }
        }
        
        
    }
