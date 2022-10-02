import java.io.*;
import java.util.Scanner;
import java.lang.Runtime;
import java.util.Random;

public class CPU {

    public static void main(String[] args){
        try {

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("java Memory.java");

            InputStream cpu_in = proc.getInputStream(); // Memory's Output Stream turns into CPU's input
            OutputStream cpu_out = proc.getOutputStream(); // Memory reads from CPU's output stream (Needs to PrintWrite & flush)

            PrintWriter pw = new PrintWriter(cpu_out);
            Scanner sc = new Scanner(cpu_in);
            // Tell Memory to copy instructions from sample files
            pw.println(args[0]);
            pw.flush();

            int timer_limit = Integer.parseInt(args[1]);

            int timer = 0; // Interrupt Counter
            // CPU Registers
            int PC = 0; int SP = 1000; int IR = 0; int AC = 0; int X = 0; int Y = 0;
            // PC = Program Counter, SP = Stack Pointer, IR = Instruction, AC, X, and Y are just for storage

            /*
                EXAMPLE:
            pw.printf("read(2)\n");
            pw.flush();
            */



            boolean kernel_mode = false;
            int MEM_output;
            boolean exit = false;
            //boolean kernel_mode = false;
            PC = 0;
            pw.printf("read(0)\n");
            pw.flush();
            while(sc.hasNextLine()){
                // PC should be incremented after an instruction fetch not when it's convenient
                // Stack pointer should point to currently the latest stack element
                MEM_output = Integer.parseInt(sc.nextLine());
                timer++;
                if(timer >= timer_limit && !kernel_mode){
                    kernel_mode = true;
                    timer -= timer_limit-1;
                    pw.printf("write(%d, %d)\n", 1999, SP);
                    pw.flush();
                    pw.printf("write(%d, %d)\n", 1998, PC);
                    pw.flush();
                    SP = 1998;
                    PC = 1000;
                    pw.printf("read(1000)\n");
                    pw.flush();
                    MEM_output = Integer.parseInt(sc.nextLine()); // Get instruction starting at 1000
                }

                PC++;
                IR = MEM_output;
                switch(MEM_output){
                    case 1: // Load the value into the AC
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        String AC_val1 = sc.nextLine();
                        int val1 = Integer.parseInt(AC_val1);

                        AC = Integer.parseInt(AC_val1);
                        break;

                    case 2: // Load the value at the address into the AC
                        pw.printf("read(%d)\n" , PC);
                        pw.flush();
                        PC++;

                        int addr2 = Integer.parseInt(sc.nextLine());
                        if(!allowAccess(addr2, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", addr2);
                            break;
                        }
                        pw.printf("read(%d)\n", addr2);
                        pw.flush();

                        int val2 = Integer.parseInt(sc.nextLine());
                        AC = val2;
                        break;
                    case 3: // Load the value from the address found in the given address into the AC
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr3 = Integer.parseInt(sc.nextLine());
                        pw.printf("read(%d)\n", addr3);
                        pw.flush();
                        int val3 = Integer.parseInt(sc.nextLine());
                        AC = val3;
                        break;

                    case 4: // Load the value at (address+X) into the AC
                        // Gets next value
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr_val4 = Integer.parseInt(sc.nextLine()) + X;
                        if(!allowAccess(addr_val4, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", addr_val4);
                            break;
                        }
                        // Gets value at new addr
                        pw.printf("read(%d)\n", addr_val4);
                        pw.flush();
                        int val4 = Integer.parseInt(sc.nextLine());
                        AC = val4;
                        break;

                    case 5: // Load the value at (address+Y) into the AC
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr_val5 = Integer.parseInt(sc.nextLine()) + Y;
                        if(!allowAccess(addr_val5, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", addr_val5);
                            break;
                        }
                        // Get value at new addr
                        pw.printf("read(%d)\n", addr_val5);
                        pw.flush();
                        int val5 = Integer.parseInt(sc.nextLine());
                        AC = val5;
                        break;

                    case 6: // Load from (SP+X) into the AC (if SP is 990, and X is 1, load from 991);
                        if(!allowAccess(SP+X, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", SP+X);
                            break;
                        }
                        pw.printf("read(%d)\n", SP+X);
                        pw.flush();
                        int val6 = Integer.parseInt(sc.nextLine());
                        AC = val6;
                        break;

                    case 7: // Store the value in the AC into the address
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr7 = Integer.parseInt(sc.nextLine());
                        if(!allowAccess(addr7, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", addr7);
                            break;
                        }
                        pw.printf("write(%d, %d)\n", addr7, AC);
                        break;

                    case 8: // Gets a random int from 1 to 100 into the AC
                        Random rand = new Random();
                        int random_num = rand.nextInt(100-1) + 1;
                        AC = random_num;
                        break;

                    case 9: // If port = 1, writes AC as an int to the screen; port = 2 writes AC as a char to the screen
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int port = Integer.parseInt(sc.nextLine());
                        if(port == 1){
                            System.out.print(AC);
                        }
                        else if(port == 2){
                            System.out.printf("%c", AC);
                        }
                        break;
                    case 10: // Add the value in X to the AC
                        AC += X;
                        break;

                    case 11: // Add the value in Y to the AC
                        AC += Y;
                        break;

                    case 12: // Subtract the value in X from the AC
                        AC -= X;
                        break;

                    case 13: // Subtract the value in Y from the AC
                        AC -= Y;
                        break;

                    case 14: // Copy the value in the AC to X
                        X = AC;
                        break;

                    case 15: // Copy the value in X to the AC
                        AC = X;
                        break;

                    case 16: // Copy the value in the AC to Y
                        Y = AC;
                        break;

                    case 17: // Copy the value in Y to the AC
                        AC = Y;
                        break;

                    case 18: // Copy the value in AC to the SP
                        SP = AC;
                        break;

                    case 19: // Copy the value in SP to the AC
                        AC = SP;
                        break;

                    case 20: // Jump to the addr
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr20 = Integer.parseInt(sc.nextLine());
                        if(!allowAccess(addr20, kernel_mode)){
                            exit = true;
                            pw.printf("exit()\n");
                            pw.flush();
                            System.out.printf("Memory violation: accessing system address %d in user mode\n", addr20);
                            break;
                        }
                        PC = addr20;
                        break;

                    case 21: // Jump to the address only if the value in the AC is zero
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr21 = Integer.parseInt(sc.nextLine());
                        //System.out.println("ADDR TO JUMP TO: " + addr21);
                        if(AC == 0){
                            if(!allowAccess(addr21, kernel_mode)){
                                exit = true;
                                pw.printf("exit()\n");
                                pw.flush();
                                System.out.printf("Memory violation: accessing system address %d in user mode\n", addr21);
                                break;
                            }
                            PC = addr21;
                        }
                        break;

                    case 22: // Jump to the address only if the value in the AC is not zero
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int addr22 = Integer.parseInt(sc.nextLine());

                        //System.out.println("ADDR TO JUMP TO: " + addr22);
                        if(AC != 0){
                            if(!allowAccess(addr22, kernel_mode)){
                                exit = true;
                                pw.printf("exit()\n");
                                pw.flush();
                                System.out.printf("Memory violation: accessing system address %d in user mode\n", addr22);
                                break;
                            }
                            PC = addr22;
                        }
                        break;

                    case 23: // Push return address onto stack, jump to the address
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int jump_addr23 = Integer.parseInt(sc.nextLine());
                        SP--;
                        pw.printf("write(%d, %d)\n", SP, PC);
                        pw.flush();
                        PC = jump_addr23;
                        break;

                    case 24: // Pop return address from the stack, jump to the address
                        pw.printf("read(%d)\n", SP);
                        pw.flush();

                        int return_addr24 = Integer.parseInt(sc.nextLine());
                        PC = return_addr24;

                        // Overwrite the value
                        pw.printf("write(%d, 0)\n" , SP);
                        pw.flush();
                        SP++;

                        break;

                    case 25: // Increment the value of X
                        X++;
                        break;

                    case 26: // Decrement the value in X
                        X--;
                        break;

                    case 27: // Push AC onto the stack
                        SP--;
                        //System.out.println(SP);
                        pw.printf("write(%d, %d)\n", SP, AC);
                        pw.flush();
                        break;

                    case 28: // Pop from stack into AC
                        // Read the value on stack pointer
                        pw.printf("read(%d)\n", SP);
                        pw.flush();

                        int val28 = Integer.parseInt(sc.nextLine());
                        AC = val28;
                        // Overwrite the value
                        pw.printf("write(%d, 0)\n" , SP);
                        pw.flush();
                        SP++;
                        break;

                    case 29: // Perform system call
                        kernel_mode = true;
                        // SP and PC registers saved on system stack
                        // Push SP and PC
                        pw.printf("write(%d, %d)\n", 1999, SP);
                        pw.flush();
                        pw.printf("write(%d, %d)\n", 1998, PC);
                        pw.flush();
                        SP = 1998;
                        PC = 1500;
                        break;

                    case 30: // Return from system call
                        if(kernel_mode){
                            kernel_mode = false;
                        }
                        pw.printf("read(%d)\n", 1998);
                        pw.flush();
                        pw.printf("write(%d, %d)\n", 1998, 0);
                        pw.flush();
                        int PC_prev = Integer.parseInt(sc.nextLine());
                        SP++;
                        pw.printf("read(%d)\n", 1999);
                        pw.flush();
                        pw.printf("write(%d, %d)\n", 1999, 0);
                        pw.flush();
                        int SP_prev = Integer.parseInt(sc.nextLine());
                        PC = PC_prev;
                        SP = SP_prev;
                        break;

                    case 50:
                        pw.printf("exit()\n");
                        pw.flush();
                        exit = true;
                        break;
                }
                if(exit){
                    break;
                }
                pw.printf("read(%d)\n", PC);
                pw.flush();
            }

            proc.waitFor();
            pw.close();
            sc.close();
            int exit_val = proc.exitValue();
            System.out.println("Process exited: " + exit_val);
        }
        catch(Throwable t){
            t.printStackTrace();
        }
    }
    public static boolean allowAccess(int addr, boolean kernel_mode){
        if(addr >= 1000 && kernel_mode == true){
            return true;
        }
        else if(addr < 1000){
            return true;
        }
        return false;
    }
}
