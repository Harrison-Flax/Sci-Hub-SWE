import java.io.BufferedWriter;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class RemoveStopWords{
    public static void main(String args[]) throws IOException {
        List<String> allStopWords = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(new File("LDAAbstractTrain.csv")));
        BufferedReader reader1 = new BufferedReader(new FileReader(new File("LDAAbstractTest.csv")));
        
        BufferedReader reader2 = new BufferedReader(new FileReader(new File("Stopwords.txt")));
        BufferedWriter writerTrain = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream("LDAAbstractTrain_rem_stopwords.csv"), "UTF-8"));
        BufferedWriter writerTest = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream("LDAAbstractTest_rem_stopwords.csv"), "UTF-8"));
       
        String line = "";
        while ((line = reader2.readLine()) != null) {
            allStopWords.add(line);
        }
        StringBuilder builder=null;
        boolean flag=false;;
        while ((line = reader.readLine()) != null) {
            String[] trainwords = line.split(" ");
            builder = new StringBuilder();
                for(String tword:trainwords){
                    flag=false;
                    for(String stopword : allStopWords) {
                     if(stopword.equalsIgnoreCase(tword.trim())) {
                        flag=true;
                     }
                   }
                if(!flag){
                    builder.append(tword.trim());
                    builder.append(' ');
                }
            }
            String lineStr=builder.toString().substring(0,builder.toString().length()-1);
            writerTrain.write(lineStr);
            writerTrain.write("\n");
        }

        while ((line = reader1.readLine()) != null) {
            String[] testwords = line.split(" ");
            builder = new StringBuilder();
            for(String tword:testwords){
                flag=false;
                for(String stopword : allStopWords) {
                    if(stopword.equalsIgnoreCase(tword.trim())) {
                        flag=true;
                    }
                }
                if(!flag){
                    builder.append(tword.trim());
                    builder.append(' ');
                }
            }
            String lineStr=builder.toString().substring(0,builder.toString().length()-1);
            writerTest.write(lineStr);
            writerTest.write("\n");
        }
    }
}