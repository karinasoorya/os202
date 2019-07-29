
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Scheduling {
	public static class Process implements Comparable<Process>{
		public Process(int A, int B, int C, int IO) {
			this.A = A;
			this.B = B;
			this.C = C;
			CPUtimeleft = C;
			this.IOburst = IO;
			this.IO = IO;
			this.state = 3;
		}
		public void resetval() {
			this.finishingtime = 0;
			this.waitingtime = 0;
			this.turnaroundtime = 0;
			this.iotime = 0;
			this.state = 3;
			this.CPUtimeleft = this.C;
			this.IOburst = this.IO;
			this.IO = IO;
			this.CPUburst = 0;
			this.cpuTime = 0;
		}
		public void printout() {
			System.out.print(A + " " + B + " " + C + " " + IO + "    ");
		}
		int real;
		int IOburst;
		int A;
		int B;
		int C;
		int IO;
		int startingtime;
		int finishingtime;
		int turnaroundtime;
		int iotime;
		int cpuTime;
		int waitingtime;
		int CPUtimeleft;
		int CPUburst;
		int g;
		int state; //0, 1 , 2, 3, 4 for ready, running, blocked, unstarted, terminated 
		public int easyprint() {
			if(this.state == 2) {
				return this.IOburst;
			}
			else if(this.state == 1) {
				return this.CPUburst;
			}
			else if(this.state == 0) {
				return 0;
			}
			else if(this.state == 4) {
				return 0;
			}
			else {
				return this.CPUburst;
			}
		}
		public int easyprint2() {
			if(this.state == 2) {
				return this.IOburst;
			}
			else if(this.state == 1) {
				return this.CPUburst;
			}
			else if(this.state == 0) {
				return this.CPUburst;
			}
			else if(this.state == 4) {
				return 0;
			}
			else {
				return 0;
			}
		}
		public String prints() {
			if(this.state == 0) {
				return "ready";
			}
			else if(this.state == 1) {
				return "running";
			}
			else if(this.state == 2) {
				return "blocked";
			}
			else if(this.state == 3) {
				return "unstarted";
			}
			else {
				return "terminated";
			}
		}
		public void randomBurst() {
			int q = randomMaker.randomOS(this.B);
			this.CPUburst = q;
			//this.CPUburst = Math.min(q, this.CPUtimeleft);
			//System.out.println(">>>>>>tttt>>>>>" + q + "x" + CPUtimeleft + " " + this.CPUburst);
		}
		public void randomIOburst() {
			this.IOburst = randomMaker.randomOS(this.IO);
		}
		@Override
		public int compareTo(Process o) {
			if(this.A > o.A) {
				return 1;
			}
			else if(this.A < o.A) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	public static class FCFS {
		public static void runFCFS(boolean verboseflag, ArrayList<Process> list1) {
			int t = 0;
			boolean done = false;

			System.out.print("The original input was: " + list1.size() + "   "); 
			for(int i = 0; i < list1.size(); i++) {
				list1.get(i).printout();
			}
			System.out.println("");
			System.out.print("The sorted input is: " + list1.size() + "   ");
			Collections.sort(list1);
			for(int i = 0; i < list1.size(); i++) {
				list1.get(i).printout();
			}
			System.out.println("\n\nThe scheduling algorithm used was FCFS\n\n");
			Queue<Process> thequeue = new LinkedList<Process>();
			boolean busy = false;
			int wc = 0;
			while(!done) {
				if(verboseflag) {
					System.out.print("\nBefore cycle " + t + " :  "); 
					for(int k = 0; k < list1.size(); k++) {
						System.out.print(list1.get(k).prints() + " " + list1.get(k).easyprint() + " "); 
					}
				}
				int terminator = 0;
				for(int i = 0; i < list1.size(); i++) {
					Process current_process = list1.get(i);
					if(current_process.A > t) {
						continue;
					}
					else if(current_process.A == t) {
						current_process.state = 0;
						thequeue.add(current_process);
					}
					else if(current_process.state == 4) {
						terminator++;
						continue;
					}
					else if(current_process.state == 1) {
						current_process.cpuTime++;
						current_process.CPUburst--;
						current_process.CPUtimeleft--;
						if(current_process.CPUtimeleft == 0) {
							current_process.state = 4;
							terminator++;
							current_process.finishingtime = t;
							busy = false;
						}
						else if(current_process.CPUburst == 0){
							current_process.state = 2;
							current_process.randomIOburst();
							busy = false;
						}
					}
					else if(current_process.state == 2) {
						
						if(current_process.IOburst == 0) {
							current_process.state = 0;
							thequeue.add(current_process);
						}
						else {
							current_process.IOburst--; 
							current_process.iotime++;
							if(current_process.IOburst == 0) {
								current_process.state = 0;
								thequeue.add(current_process);
							}
						}
					}
				}
				if(!busy && !thequeue.isEmpty()) {
					Process curr = thequeue.poll();
					curr.randomBurst();
					busy = true;
					curr.state = 1;
				
				}
				t++;
				for(int p = 0; p < list1.size(); p++) {
					if(list1.get(p).state == 2) {
						wc++;
						break;
					}
				}
				done = terminator == list1.size();
			}

			int finish = -1;
			double averagewait = 0;
			double averageturnaround = 0;
			int totalio = 0;
			int cpuUse = 0;
			for(int i = 0; i < list1.size(); i++) {
				Process curr = list1.get(i);
				int a = curr.A;
				int b = curr.B;
				int c = curr.C;
				int io = curr.IO;
				int cpu = curr.cpuTime;
				cpuUse += cpu;
				curr.turnaroundtime = curr.finishingtime - a;
				curr.waitingtime = curr.turnaroundtime - curr.C - curr.iotime;
				averagewait += list1.get(i).waitingtime;
				averageturnaround += list1.get(i).finishingtime - a;
				totalio += list1.get(i).iotime;
				if(curr.finishingtime > finish) {
					finish = curr.finishingtime;
				}
				System.out.println("Process " + i + ": \n    (A,B,C,IO) = (" + a + "," + b + "," + c + "," + io + ")");
				System.out.println("    Finishing Time: " + list1.get(i).finishingtime);
				System.out.println("    Turnaround Time: " + list1.get(i).turnaroundtime);
				System.out.println("    I/O Time: " + list1.get(i).iotime);
				System.out.println("    Waiting Time: " + list1.get(i).waitingtime);
			}
			System.out.println("Summary Data:" );
			System.out.println("    Finishing Time: " + finish);
			System.out.println("    CPU Utilization: " + ((double)cpuUse)/finish);
			System.out.println("    I/O Utilization: " + ((double)wc)/finish);
			System.out.println("    Throughput: " + (((double)list1.size())/finish)*100 + " processes per hundred cycles");
			System.out.println("    Average turnaround time: " + averageturnaround/list1.size());
			System.out.println("    Average waiting time: " + averagewait/list1.size());

		}
	}
	public static class RR {
		public static void runRR(boolean verboseflag, ArrayList<Process> list2) {
			int q = 2;
			boolean done = false;
			int t = 0;
			System.out.print("The original input was: " + list2.size() + "   "); 
			for(int i = 0; i < list2.size(); i++) {
				list2.get(i).resetval();
				list2.get(i).printout();
			}
			System.out.println("");
			System.out.print("The sorted input is: " + list2.size() + "   ");
			Collections.sort(list2);
			for(int i = 0; i < list2.size(); i++) {
				list2.get(i).printout();
			}


			boolean busy = false;
			Queue<Process> thequeue = new LinkedList<Process>();
			System.out.println("\n\nThe scheduling algorithm used was RR\n\n");

			int wc = 0;
			while(!done) {
				if(verboseflag) {
					System.out.print("\nBefore cycle " + t + " :  "); 
					for(int k = 0; k < list2.size(); k++) {
						System.out.print(list2.get(k).prints() + " " + list2.get(k).easyprint() + " "); 
					}
				}
				int terminator = 0;
				for(int i = 0; i < list2.size(); i++) {
					Process current_process = list2.get(i);
					if(current_process.A > t) {
						continue;
					}
					else if(current_process.A == t) {
						current_process.state = 0;
						thequeue.add(current_process);

					}
					else if(current_process.state == 4) {
						terminator++;
						continue;
					}
					else if(current_process.state == 1) {
						current_process.cpuTime++;

						current_process.CPUburst--;
						current_process.CPUtimeleft--;

						if(current_process.CPUtimeleft == 0) {
							current_process.state = 4;
							terminator++;
							current_process.finishingtime = t;
							busy = false;
						}
						else if(current_process.CPUburst == 0){
							if(current_process.real > 0) {
								current_process.state = 0;
								thequeue.add(current_process);
								busy = false;
							}
							else {
								current_process.state = 2;
								current_process.iotime++;
								current_process.randomIOburst();
								busy = false;
							}
						}
					}
					else if(current_process.state == 2) {
						if(current_process.IOburst == 1) {
							current_process.state = 0;
							thequeue.add(current_process);
						}
						else {
							current_process.IOburst--; 
							current_process.iotime++;
						}
					}

				}


				if(!busy && !thequeue.isEmpty()) {
					Process curr = thequeue.poll();

					if(curr.real == 0) {
						curr.randomBurst();
						curr.real = curr.CPUburst;
					}

					if(curr.real > 2) {
						curr.CPUburst = 2;
						curr.real -= 2;
					}
					else {
						curr.CPUburst = curr.real;
						curr.real = 0;
					}

					busy = true;
					curr.state = 1;
				}
				t++;
				for(int p = 0; p < list2.size(); p++) {
					if(list2.get(p).state == 2) {
						wc++;
						break;
					}
				}
				done = terminator == list2.size();
			}

			int finish = -1;
			int cpuUse = 0;
			int totalio = 0;
			double averagewait = 0;
			double averageturnaround = 0;
			for(int i = 0; i < list2.size(); i++) {
				Process curr = list2.get(i);
				int a = curr.A;
				int b = curr.B;
				int c = curr.C;
				int io = curr.IO;
				int cpu = curr.cpuTime;
				cpuUse += cpu;
				curr.turnaroundtime = curr.finishingtime - a;
				curr.waitingtime = curr.turnaroundtime - curr.C - curr.iotime;
				averagewait += list2.get(i).waitingtime;
				averageturnaround += list2.get(i).finishingtime - a;
				totalio += list2.get(i).iotime;
				if(curr.finishingtime > finish) {
					finish = curr.finishingtime;
				}
				System.out.println("");
				System.out.println("Process " + i + ": \n    (A,B,C,IO) = (" + a + "," + b + "," + c + "," + io + ")");
				System.out.println("    Finishing Time: " + list2.get(i).finishingtime);
				System.out.println("    Turnaround Time: " + list2.get(i).turnaroundtime);
				System.out.println("    I/O Time: " + list2.get(i).iotime);
				System.out.println("    Waiting Time: " + list2.get(i).waitingtime);
			}
			System.out.println("Summary Data:" );
			System.out.println("    Finishing Time: " + finish);
			System.out.println("    CPU Utilization: " + ((double)cpuUse)/finish);
			System.out.println("    I/O Utilization: " + ((double)wc)/finish);
			System.out.println("    Throughput: " + (((double)list2.size())/finish)*100 + " processes per hundred cycles");
			System.out.println("    Average turnaround time: " + averageturnaround/list2.size());
			System.out.println("    Average waiting time: " + averagewait/list2.size());

		}
	}
	public static class Uniprogrammed {
		public static void runUniprogrammed(boolean verboseflag, ArrayList<Process> list3) {
			boolean done = false;
			int t = 0;
			int cpuUse = 0;
			System.out.print("The original input was: " + list3.size() + "   "); 
			for(int i = 0; i < list3.size(); i++) {
				list3.get(i).resetval();
				list3.get(i).printout();
			}
			System.out.println(" ");
			System.out.print("The sorted input is: " + list3.size() + "   ");
			Collections.sort(list3);
			for(int i = 0; i < list3.size(); i++) {
				list3.get(i).printout();
			}
			boolean busy = false;
			Queue<Process> thequeue = new LinkedList<Process>();
			int wc = 0;
			System.out.println("\n\nThe scheduling algorithm used was Uniprogrammed\n\n");
			while(!done) {
				if(verboseflag) {
					System.out.print("\nBefore cycle " + t + " :  "); 
					for(int k = 0; k < list3.size(); k++) {
						System.out.print(list3.get(k).prints() + " " + list3.get(k).easyprint() + " "); 
					}
				}
				int terminator = 0;
				for(int i = 0; i < list3.size(); i++) {
					Process current_process = list3.get(i);
					if(current_process.A > t) {
						continue;
					}
					else if(current_process.A == t) {
						current_process.state = 0;
						thequeue.add(current_process);   
					}
					else if(current_process.state == 4) {
						terminator++;
						continue;
					}
					else if(current_process.state == 1) {
						current_process.cpuTime++;
						current_process.CPUburst--;
						current_process.CPUtimeleft--;
						if(current_process.CPUtimeleft == 0) {
							current_process.state = 4;
							current_process.finishingtime = t;
							terminator++;
							busy = false;
						}
						else if(current_process.CPUburst == 0){
							current_process.state = 2;
							current_process.iotime++;
							current_process.randomIOburst();
						}
					}
					else if(current_process.state == 2) {
						if(current_process.IOburst == 1) {
							current_process.state = 1;
							current_process.randomBurst();
						}
						else {
							current_process.IOburst--; 
							current_process.iotime++;
						}
					}
				}
				if(!busy && !thequeue.isEmpty()) {
					Process curr = thequeue.poll();
					curr.randomBurst();
					busy = true;
					curr.state = 1;
				}
				t++;
				for(int p = 0; p < list3.size(); p++) {
					if(list3.get(p).state == 2) {
						wc++;
						break;
					}
				}
				done = terminator == list3.size();
			}
			int finish = -1;
			double averagewait = 0;
			double averageturnaround = 0;
			for(int i = 0; i < list3.size(); i++) {
				Process curr = list3.get(i);
				int a = curr.A;
				int b = curr.B;
				int c = curr.C;
				int io = curr.IO;
				curr.turnaroundtime = curr.finishingtime - curr.A;
				averageturnaround += list3.get(i).finishingtime - a;
				curr.waitingtime = curr.turnaroundtime - curr.C - curr.iotime;
				averagewait += list3.get(i).waitingtime;
				if(curr.finishingtime > finish) {
					finish = curr.finishingtime;
				}
				int cpu = curr.cpuTime;
				cpuUse += cpu;
				System.out.println("Process " + i + ": \n    (A,B,C,IO) = (" + a + "," + b + "," + c + "," + io + ")");
				System.out.println("    Finishing Time: " + list3.get(i).finishingtime);
				System.out.println("    Turnaround Time: " + list3.get(i).turnaroundtime);
				System.out.println("    I/O Time: " + list3.get(i).iotime);
				System.out.println("    Waiting Time: " + list3.get(i).waitingtime);
			}
			System.out.println("Summary Data:" );
			System.out.println("    Finishing Time: " + finish);
			System.out.println("    CPU Utilization: " + ((double)cpuUse)/finish);
			System.out.println("    I/O Utilization: " + ((double)wc)/finish);
			System.out.println("    Throughput: " + (((double)list3.size())/finish)*100 + " processes per hundred cycles");
			System.out.println("    Average turnaround time: " + averageturnaround/list3.size());
			System.out.println("    Average waiting time: " + averagewait/list3.size());
		}
	}
	public static class SRTN {
		public static void runSRTN(boolean verboseflag, ArrayList<Process> list1) {
			int t = 0;
			boolean done = false;
			int cpuUse = 0;
			int ioUse = 0;
			System.out.print("The original input was: " + list1.size() + "   "); 
			for(int i = 0; i < list1.size(); i++) {
				list1.get(i).resetval();
				list1.get(i).printout();
			}
			System.out.println("");
			System.out.print("The sorted input is: " + list1.size() + "   ");
			Collections.sort(list1);
			for(int i = 0; i < list1.size(); i++) {
				list1.get(i).g = i;
				list1.get(i).resetval();
				list1.get(i).printout();
			}
			boolean busy = false;
			Comparator<Process> peejuh = new Comparator<Process>() {
				@Override
				public int compare(Process o1, Process o2) {
					if(o1.C < o2.C) {
						return -1;
					}
					else if(o1.C > o2.C) {
						return 1;
					}
					else {
						int tmp = o1.compareTo(o2);
						if(tmp == 0) {
							if(o1.g < o2.g) {
								return -1;
							}
							else if(o1.g > o2.g) {
								return 1;
							}
						}
						return tmp;
					}
				}
			};
			PriorityQueue<Process> thequeue = new PriorityQueue<Process>(list1.size(), peejuh);
			System.out.println("\n\nThe scheduling algorithm used was SRTN\n\n");
			Process aprocess = null;
			int wc = 0;
			while(!done) {
				if(verboseflag) {
					System.out.print("\nBefore cycle " + t + " :  "); 
					for(int k = 0; k < list1.size(); k++) {
						System.out.print(list1.get(k).prints() + " " + list1.get(k).easyprint2() + " "); 
					}
				}
				int terminator = 0;
				for(int i = 0; i < list1.size(); i++) {
					Process current_process = list1.get(i);
					if(current_process.A > t) {
						continue;
					}
					else if(current_process.A == t) {
						current_process.state = 0;
						thequeue.add(current_process);
					}
					else if(current_process.state == 4) {
						terminator++;
						continue;
					}
					else if(current_process.state == 1) {
						current_process.cpuTime++;
						current_process.CPUburst--;
						current_process.CPUtimeleft--;
						if(current_process.CPUtimeleft == 0) {
							current_process.state = 4;
							current_process.finishingtime = t;
							terminator++;
							thequeue.remove(current_process);
							aprocess = null;
							busy = false;
						}
						else if(current_process.CPUburst == 0){
							current_process.state = 2;
							current_process.iotime++;
							thequeue.remove(current_process);
							aprocess = null;
							current_process.randomIOburst();
							busy = false;
						}
					}
					else if(current_process.state == 2) {
						if(current_process.IOburst == 1) {
							current_process.state = 0;
							thequeue.add(current_process);
						}
						else {
							current_process.IOburst--; 
							current_process.iotime++;
						}
					}
				}
				if(!thequeue.isEmpty()) {
					Process curr = thequeue.peek();
					/*System.out.print("\nNext to run: ");
					curr.printout();
					System.out.println("\n\nthequeue: ");
					for(Process foo : thequeue) {
						foo.printout();
					}
					System.out.println("");*/
					if(!curr.equals(aprocess)) {
						if(aprocess!=null) {
							aprocess.state = 0;
						}
						if(curr.CPUburst == 0) {
							curr.randomBurst();
						}
					}
					busy = true;
					curr.state = 1;
					aprocess = curr;
				}				
				t++;
				for(int p = 0; p < list1.size(); p++) {
					if(list1.get(p).state == 2) {
						wc++;
						break;
					}
				}
				done = terminator == list1.size();
			}
			int finish = -1;
			double averagewait = 0;
			double averageturnaround = 0;
			for(int i = 0; i < list1.size(); i++) {
				Process curr = list1.get(i);
				int a = curr.A;
				int b = curr.B;
				int c = curr.C;
				int io = curr.IO;
				curr.turnaroundtime = curr.finishingtime - curr.A;
				averageturnaround += list1.get(i).finishingtime - a;
				curr.waitingtime = curr.turnaroundtime - curr.C - curr.iotime;
				averagewait += list1.get(i).waitingtime;
				if(curr.finishingtime > finish) {
					finish = curr.finishingtime;
				}
				int cpu = curr.cpuTime;
				cpuUse += cpu;
				System.out.println("Process " + i + ": \n    (A,B,C,IO) = (" + a + "," + b + "," + c + "," + io + ")");
				System.out.println("    Finishing Time: " + list1.get(i).finishingtime);
				System.out.println("    Turnaround Time: " + list1.get(i).turnaroundtime);
				System.out.println("    I/O Time: " + list1.get(i).iotime);
				System.out.println("    Waiting Time: " + list1.get(i).waitingtime);
			}
			System.out.println("Summary Data:" );
			System.out.println("    Finishing Time: " + finish);
			System.out.println("    CPU Utilization: " + ((double)cpuUse/finish));
			System.out.println("    I/O Utilization: " + ((double)wc)/finish);
			System.out.println("    Throughput: " + (((double)list1.size())/finish)*100 + " processes per hundred cycles");
			System.out.println("    Average turnaround time: " + averageturnaround/list1.size());
			System.out.println("    Average waiting time: " + averagewait/list1.size());
		}
	}
	public static class randomMaker {
		public static int i = 0;
		public static ArrayList<Integer> numbers = new ArrayList<Integer>();
		private static Scanner sc = new Scanner(System.in);
		public static final String filepath = "random-numbers.txt";


		public static void init() {
			File f = new File(filepath);
			try{
				sc = new Scanner(f);
				while(sc.hasNext()) {
					numbers.add(sc.nextInt());
				}
			}
			catch(FileNotFoundException e) {}
		}

		public static void reset() {
			i = 0;
		}

		public static int randomOS(int upper) {
			return 1 + (numbers.get(i++) % upper);
		} 
	}
	public static void main(String[] args) {
		randomMaker.init();
		File newfile;
		boolean verboseflag = false;
		ArrayList<Process> theprocesses = new ArrayList<Process>();
		if(args[0].equals("--verbose")) {
			verboseflag = true;
			newfile = new File(args[1]);
		}
		else {
			newfile = new File(args[0]);
		}
		Scanner scanner = new Scanner(System.in);
		try {
			scanner = new Scanner(newfile);
			int numprocesses = scanner.nextInt();
			while(numprocesses > 0) {
				Process aprocess = new Process(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
				theprocesses.add(aprocess);
				numprocesses--;
			}
			ArrayList<Process> dup = new ArrayList<>();
			for(int i = 0; i < theprocesses.size(); i++) {
				dup.add(theprocesses.get(i));
			}
			FCFS.runFCFS(verboseflag, dup);
			randomMaker.reset();
			dup.clear();
			for(int i = 0; i < theprocesses.size(); i++) {
				dup.add(theprocesses.get(i));
			}
			System.out.println("********");
			RR.runRR(verboseflag, dup);
			randomMaker.reset();
			dup.clear();
			for(int i = 0; i < theprocesses.size(); i++) {
				dup.add(theprocesses.get(i));
			}
			System.out.println("********");
			Uniprogrammed.runUniprogrammed(verboseflag, dup);
			randomMaker.reset();
			dup.clear();
			for(int i = 0; i < theprocesses.size(); i++) {
				dup.add(theprocesses.get(i));
			}
			System.out.println("********");
			SRTN.runSRTN(verboseflag, dup);
			randomMaker.reset();

		} catch (FileNotFoundException e) {
			System.err.println("File doesn't exist.");
		}
		System.out.println("the size of the file is: + " + randomMaker.numbers.size());
	}
}
