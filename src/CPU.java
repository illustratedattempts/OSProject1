import java.io.*;
import java.util.Scanner;
import java.lang.Runtime;

public class CPU {

    public static void main(String[] args){
        try {

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("java src/Memory.java");

            InputStream cpu_in = proc.getInputStream(); // Memory's Output Stream turns into CPU's input
            OutputStream cpu_out = proc.getOutputStream(); // Memory reads from CPU's output stream (Needs to PrintWrite & flush)

            PrintWriter pw = new PrintWriter(cpu_out);
            Scanner sc = new Scanner(cpu_in);
            // Tell Memory to copy instructions from sample files
            pw.printf("%s\n", args[0]);
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
                    //System.out.println("TIMER! INTERRUPTED AT PC = " + PC + " SAVED SP AT = " + SP); System.out.println();
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

                //System.out.println("PC: " + PC);
                PC++;
                //System.out.println("IR: " + MEM_output);
                IR = MEM_output;
                switch(MEM_output){
                    case 1: // Load the value into the AC
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        String AC_val1 = sc.nextLine();
                        int val1 = Integer.parseInt(AC_val1);

                        //System.out.println("Loaded value: " + AC_val1);
                        AC = Integer.parseInt(AC_val1);
                        //System.out.println("AC = " + AC); System.out.println();
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
                        //System.out.println("LOAD FROM ADDRESS: " + addr2 + " AC = " + AC); System.out.println();
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
                        //System.out.println("AC = " + AC); System.out.println();
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
                        //System.out.println("AC = " + AC); System.out.println();
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
                        //System.out.println(SP+X);
                        pw.flush();
                        int val6 = Integer.parseInt(sc.nextLine());
                        AC = val6;
                        //System.out.println("[LOAD SP+X] SP = " + SP + " | X = " + X + " | AC = " + AC); System.out.println();
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
                        //System.out.println("[LOAD AC VALUE INTO ADDR] ADDR: " + addr7 + " AC: " + AC);
                        break;

                    case 8:


                    case 9: // If port = 1, writes AC as an int to the screen; port = 2 writes AC as a char to the screen
                        pw.printf("read(%d)\n", PC);
                        pw.flush();
                        PC++;

                        int port = Integer.parseInt(sc.nextLine());
                        if(port == 1){
                            //System.out.println("OUTPUT = " + AC);
                            System.out.print(AC);
                        }
                        else if(port == 2){
                           //System.out.printf("OUTPUT = %c\n\n", AC);
                            System.out.printf("%c", AC);
                        }
                        else{
                           // System.out.println();
                        }
                        break;
                    case 10: // Add the value in X to the AC
                        AC += X;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 11: // Add the value in Y to the AC
                        AC += Y;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 12: // Subtract the value in X from the AC
                        AC -= X;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 13: // Subtract the value in Y from the AC
                        AC -= Y;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 14: // Copy the value in the AC to X
                        X = AC;
                        //System.out.println("X = " + X); System.out.println();
                        break;

                    case 15: // Copy the value in X to the AC
                        AC = X;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 16: // Copy the value in the AC to Y
                        Y = AC;
                        //System.out.println("Y = " + Y); System.out.println();
                        break;

                    case 17: // Copy the value in Y to the AC
                        AC = Y;
                        //System.out.println("AC = " + AC); System.out.println();
                        break;

                    case 18: // Copy the value in AC to the SP
                        SP = AC;
                        //System.out.println("SP = " + SP); System.out.println();
                        break;

                    case 19: // Copy the value in SP to the AC
                        AC = SP;
                        //System.out.println("AC = " + AC); System.out.println();
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
                        //System.out.println("[JUMP] PC = " + PC); System.out.println();
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
                            //System.out.println("PC = " + PC); System.out.println();
                        }
                       //System.out.println();
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
                            //System.out.println("PC = " + PC); System.out.println();
                        }
                        //System.out.println();
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
                        //System.out.println("[PUSH STACK and JUMP] WRITTEN TO SP = " + SP + " | JUMP Address = " + jump_addr23 + " | PC = " + PC); System.out.println();
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
                        //System.out.println("[POP STACK AND JUMP TO POPPED] SP = " + SP + " | VALUE = " + return_addr24 + " | PC = " + PC); System.out.println();
                        break;

                    case 25: // Increment the value of X
                        X++;
                        //System.out.println("X = " + X); System.out.println();
                        break;

                    case 26: // Decrement the value in X
                        X--;
                        //System.out.println("X = " + X); System.out.println();
                        break;

                    case 27: // Push AC onto the stack
                        SP--;
                        //System.out.println(SP);
                        pw.printf("write(%d, %d)\n", SP, AC);
                        pw.flush();
                        //System.out.println("[PUSH AC TO STACK] WRITTEN TO SP = " + SP + " | AC = " + AC); System.out.println();
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
                        //System.out.println("[POP FROM STACK INTO AC] SP = " + SP + " | VALUE = " + val28 + " | AC = " + AC); System.out.println();
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

                    default:
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
