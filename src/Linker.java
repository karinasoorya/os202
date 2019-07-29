import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class Linker {
	static ArrayList<module> modules = new ArrayList<module>();
	static ArrayList<symbol> allsyms = new ArrayList<symbol>();
	static ArrayList<String> alluses = new ArrayList<String>();
	static ArrayList<instruction> allinstructions = new ArrayList<instruction>();
	static int num_modules = 0;
	static int num_definition = 0;
	static int num_uses = 0;
	static int curr_module = 0;
	static int num_instructions = 0;
	public static class module {
		ArrayList<symbol> syms = new ArrayList<symbol>();
		ArrayList<String> uses = new ArrayList<String>();
		ArrayList<instruction>  instructions = new ArrayList<instruction>();
		int memoryspace = 200; 
		int initial;
		int ending;
		int size;
		int num_e = 0;
		public module(int initial) {
			this.initial = initial;
			this.ending = initial;
			this.size = 0;
		}
	}
	public static class symbol {
		String sym;
		int module_defined;
		Integer value;
		Integer rl;
		boolean isUsed = false;
		boolean isDefined = true;
		public symbol(String sym, Integer value) {
			this.sym = sym;
			this.value = value;
		}
	}
	public static class instruction {
		boolean isFixed = false;
		char type;
		Integer instruct;
		Integer real_location;
		public instruction(char type, Integer instruct) {
			this.type = type;
			this.instruct = instruct;
		}
	}
	public static void passOne(Scanner info) {
		num_modules = info.nextInt();
		while(curr_module < num_modules && info.hasNext()) {
			boolean flag = true;
			module new_module = new module(0);
			num_definition = info.nextInt(); 
			for(int i = 0; i< num_definition; i++) {
				symbol current_sym = new symbol(info.next(), info.nextInt());
				for(int j = 0; j < allsyms.size(); j++) {
					if(allsyms.get(j).sym.equals(current_sym.sym)) {
						flag = false;
						System.out.println("Warning: Symbol " + current_sym.sym + " was already defined.");
					}
				}
				if(curr_module > 0) {
					current_sym.value = allinstructions.size()  + current_sym.value;
				}
				if(flag) {
					current_sym.module_defined = curr_module;
					allsyms.add(current_sym);
					new_module.syms.add(current_sym);
				}
			}
			num_uses = info.nextInt();
			for(int i = 0; i< num_uses; i++) {
				String current_use = info.next();
				alluses.add(current_use);
				new_module.uses.add(current_use);
			}
			num_instructions = info.nextInt();
			for(int i = 0; i < allsyms.size(); i++) {
				symbol current_sym = allsyms.get(i);
				if(curr_module > 0) {
					if(current_sym.value - allinstructions.size() > num_instructions) {
						current_sym.value = 0; 
						System.out.println("Warning: the definition of " + current_sym.sym + " exceeds the module size, zero used.");
					}
				}
			}
			for(int i = 0; i < num_instructions; i++) {
				char c = info.next().charAt(0);
				int current_instruction = info.nextInt();
				instruction instruct = new instruction(c, current_instruction);
				allinstructions.add(instruct);
				new_module.instructions.add(instruct);
				if(c == 'E') {
					new_module.num_e++;
				}
			}
			modules.add(new_module);
			curr_module++;
		}
	}
	public static void show() {
		System.out.println(" ");
		System.out.println("Symbol Table:");
		for(int i = 0; i < allsyms.size(); i++) {
			if(allsyms.get(i).isDefined) {
				System.out.println("   " + allsyms.get(i).sym + "=" + allsyms.get(i).value);
			}
		}
		System.out.println(" ");
		System.out.println("Memory Map: ");
		int q = 0;
		while(q<allinstructions.size()) {
			for(int i = 0; i < modules.size(); i++) {
				module curr_mod = modules.get(i);
				for(int k = 0; k < curr_mod.instructions.size(); k++) {
					int curr_instruct = curr_mod.instructions.get(k).instruct;
					System.out.println(" " + q + ": " + curr_instruct);
					q++;
				}
			}
		} 
	}
	public static void passTwo(Scanner info) {
		for(int i = 0; i < modules.size(); i++) {
			module current_module = modules.get(i);
			for(int j = 0; j < current_module.instructions.size(); j++) {
				instruction current_instruction = current_module.instructions.get(j); 
				if(current_instruction.type == 'E') {
					int index_use = get_address(current_instruction.instruct);
					if(index_use > current_module.uses.size() - 1) {
						System.out.println("Warning: External address " + current_instruction.instruct + " is too large to reference the use list! Treating this address as immediate.");
					} else {
						String val = current_module.uses.get(index_use);
						int k = 0;
						boolean f = true;
						while(k < allsyms.size()) {
							if(allsyms.get(k).sym.equals(val)) {
								allsyms.get(k).isUsed = true;
								current_instruction.instruct = get_leading_digits(current_instruction.instruct);
								current_instruction.instruct = current_instruction.instruct + allsyms.get(k).value;
								f = false;
								break;
							}
							else {
								k++;
							}
						}
						if(f) {
							symbol sy = new symbol(val, 0);
							sy.isDefined = false;
							allsyms.add(sy);
						}
					}
				}
				else if(current_instruction.type == 'R') {
					int fixer = 0;
					if(get_address(current_instruction.instruct) <= current_module.instructions.size()) {
						for(int q = 0; q < i; q++) {
							fixer += modules.get(q).instructions.size();
						}
						current_instruction.instruct = current_instruction.instruct + fixer;
					} else {
						System.out.println("Warning: Relative address " + current_instruction.instruct + " exceeds the size of this module. Zero used.");
						current_instruction.instruct = get_leading_digits(current_instruction.instruct);
					}	
				}
				else if(current_instruction.type == 'A') {
					int stuff = get_address(current_instruction.instruct);
					if(stuff > 800) {

						System.out.println("Warning: The absolute address " + current_instruction.instruct + " exceeds the machine size, zero used.");
						current_instruction.instruct = get_leading_digits(current_instruction.instruct);
					}
				}
			}
		}
		for(int i = 0; i < allsyms.size(); i++) {
			if(allsyms.get(i).isDefined && !allsyms.get(i).isUsed) {
				System.out.println("Warning: " + allsyms.get(i).sym + " was defined, but not used.");
			}
			if(!allsyms.get(i).isDefined) {
				System.out.println("Warning: " + allsyms.get(i).sym + " was used, but not defined. Default value of zero was used.");
			} 
		}
	}
	public static int get_address(int x) {
		int i = 0;
		int address = 0;
		while(i < 3) {
			int digit = x % 10; 
			address = address + digit * (int)(Math.pow(10, i));
			x = x/10;
			i++;
		}
		return address; 
	}
	public static int get_leading_digits(int x) {
		int i = 0;
		int digit = 0;
		int lead = 0;
		while(i < 4) {
			if(i == 3) {
				digit = x % 10;
			}
			x = x / 10;
			lead = lead + digit * (int)Math.pow(10, i);
			i++;
		}
		return lead;
	}
	public static void main(String[] args) {
		Scanner f = new Scanner(System.in);
		String filename = f.next();
		File file = new File(filename); 
		try {
			Scanner stuff = new Scanner(System.in);
			stuff = new Scanner(file);
			passOne(stuff);
			passTwo(stuff);
			show();
		}
		catch(FileNotFoundException e) {
			System.err.println("The file wasn't found.");
		}
	}
}	
