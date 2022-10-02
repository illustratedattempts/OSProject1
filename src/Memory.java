import java.util.Scanner;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Memory {
    public static void main(String[] args){
        try{
            int[] int_mem = new int[2000];
            Scanner sc = new Scanner(System.in);
            // Load instruction values into memory
            Pattern digit_pat = Pattern.compile("(^\\d+)"); // Regex for purely digits - GROUP 1
            Pattern move_pat = Pattern.compile("\\.(\\d+)"); // Regex for '.' followed by digits - GROUP 1
            String file_name = "";
            if(sc.hasNextLine()){
                file_name = sc.nextLine();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file_name));
            String line = reader.readLine();
            int pos = 0;
            //System.out.println("SYSTEM READING:");
            while(line != null){
                if( (line.length() > 0) && (line.charAt(0) == '.') ){ // Changes the pointer of the memory
                    Matcher mem = move_pat.matcher(line);
                    if(mem.find()){
                        pos = Integer.parseInt(mem.group(1));
                    }
                }
                else {
                    Matcher mem = digit_pat.matcher(line);
                    if (mem.find()) { // Places digit into memory and increments pointer
                        int_mem[pos] = Integer.parseInt(mem.group(1));
                        pos++;
                        //System.out.println(m.group(1));
                    }

                }
                line = reader.readLine();
            }
            /*
            System.out.println("------------------------------------------------------------");
            System.out.println("INTERNAL MEMORY:");
            for(int i = 0; i < int_mem.length; i++){
                System.out.println(i + ": " + int_mem[i]);
            }
            */


            //Read and Write Operation Patterns
            Pattern read_op = Pattern.compile("read\\((\\d+)\\)");
            Pattern write_op = Pattern.compile("write\\((\\d+),\\s?(\\d+)\\)");

            //Take CPU input
            // NEED EXIT CALL TO BREAK FOR BOTH CPU AND MEM
            String CPU_input;
            while(sc.hasNextLine()){
                CPU_input = sc.nextLine();
                Matcher r = read_op.matcher(CPU_input);
                Matcher w = write_op.matcher(CPU_input);
                if(r.find()){
                    int addr = Integer.parseInt(r.group(1));
                    System.out.println(int_mem[addr]);
                }
                else if(w.find()){
                    int addr = Integer.parseInt(w.group(1));
                    int data = Integer.parseInt(w.group(2));
                    int_mem[addr] = data;
                }
                else if(CPU_input.equals("exit()")){
                    break;
                }
            }
            reader.close();
            sc.close();

        }
        catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
