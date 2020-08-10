import java.util.*;
import java.lang.*;
import java.io.*;

/*program reads 3 files named client.csv, taxis.csv, nodes.csv (so in 
order to run it with our alternative input files change their name 
accordingly) and only one  argument which is the maximum length 
of the openset*/

public class tn {

    public static void main(String[] args) {
		try{ 	
			int NoT=0;	
			//NoT=number of taxis+1
			try (BufferedReader br = new BufferedReader(new FileReader("taxis.csv"))) {
				while (br.readLine() != null) {
					NoT++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			person[] P = new person[NoT];		    //0: client | rest: taxis
			HashMap<String, node> N = new HashMap<String, node>();
			double[] min = new double[NoT];
			node[] closest = new node[NoT];
			double minCost, pathCost;
			int i, winnerId=-1, BOUND,cnt=0,max=0;
			int[] count = new int[NoT];
			int[] max_len = new int[NoT];
			node last;
			
			parse(args, P, N);				        //parse and return full arrays/Hashmaps
			 
			theONE(P, N, min, closest, NoT);	    //find nodes closest to client and taxis
			
			BOUND = Integer.parseInt(args[0]);
			minCost = Double.MAX_VALUE;			
			for (i=1; i<NoT; i++) {
				last = Astar(N, closest[i], closest[0], BOUND,count,max_len,i);
				cnt+=count[i];
				if(max<max_len[i])max=max_len[i];
				if (last == null) continue;

				pathCost = min[i] + (last.getG() + last.getH()) + min[0];
				if (pathCost < minCost) {
					minCost = pathCost;
					winnerId = P[i].getId();
				}

				kml(P[0], P[i], closest[i], last, BOUND, NoT);
				
				clearNodes(N);
			}
			
			System.out.println("max=" + max + ", epanal=" + cnt);		 //print actual maximum length of openSet and number of steps
			
			if(winnerId==-1){
				System.out.println("Failure");
				return;
			}
			
			System.out.println("Taxi ID: " + winnerId + ", Distance traveled: " + minCost + " meters");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}
		
	public static void parse(String[] args, person[] P, HashMap<String, node> N)	{		//parse and return full arrays		
        String line = "";
        String cvsSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader("client.csv"))) {
			br.readLine();
            while ((line = br.readLine()) != null) {
				// use comma as separator
                String[] in = line.split(cvsSplitBy);
				P[0] = new person(Double.parseDouble(in[0]), Double.parseDouble(in[1]), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
		}
		
        int i=1;
		try (BufferedReader br = new BufferedReader(new FileReader("taxis.csv"))) {
			br.readLine();
            while ((line = br.readLine()) != null) {
				// use comma as separator
                String[] in = line.split(cvsSplitBy);
				P[i] = new person(Double.parseDouble(in[0]), Double.parseDouble(in[1]), Integer.parseInt(in[2]));
				i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader("nodes.csv"))) {
			node current, prev;
			String key;
			int pr_id;
			
			br.readLine();
			line = br.readLine();
			
			String[] in = line.split(cvsSplitBy);
			current = new node(Double.parseDouble(in[0]), Double.parseDouble(in[1]), P[0].getx(), P[0].gety());
			key = in[0] + " " + in[1];
			N.put(key, current);
			
			prev = current;
			pr_id = Integer.parseInt(in[2]);
			
            while ((line = br.readLine()) != null) {
                in = line.split(cvsSplitBy);
				key = in[0] + " " + in[1];
				current = N.get(key);
				if (current == null) current = new node(Double.parseDouble(in[0]), Double.parseDouble(in[1]), P[0].getx(), P[0].gety());
				
				if (pr_id == Integer.parseInt(in[2])) {
					current.neighbours.add(prev);
					prev.neighbours.add(current);
				}
				
				N.put(key, current);
				
				pr_id = Integer.parseInt(in[2]);
				prev = current;

            }
			
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	public static void theONE(person[] P, HashMap<String, node> N, double[] min, node[] closest, int NoT) {		//find node closest to client and taxis
		try {
			double check;
			node current;
			int i;
			
			for (i=0; i<NoT; i++) min[i] = Double.MAX_VALUE;
			
			for (HashMap.Entry<String, node> entry : N.entrySet()) {
				current = entry.getValue();
				
				check = current.getH();
				if (check < min[0]) {
					min[0] = check;
					closest[0] = current;
				}
				
				for (i=1; i<NoT; i++) {
					check = current.distFrom(P[i].getx(), P[i].gety());
					if (check < min[i]) {
						min[i] = check;
						closest[i] = current;
					}
				}
			}
		} catch (Exception e) {
            e.printStackTrace();
		}		
	}

	public static node Astar(HashMap<String, node> N, node start, node goal, int BOUND,int[] count, int[] length, int id) {
		try {
			TreeSet<node> openSet = new TreeSet<node>();
			ArrayList<node> neighs;
			node current, child;
			double dist;
			int i;
			length[id]=0;
			count[id]=0;
			start.setGF(null);
			openSet.add(start);
			
			while (!openSet.isEmpty()) {
				count[id]++;
				current = openSet.pollFirst();				//get first and remove
				if (current.equals(goal)) return current;
				
				neighs = current.getNeigh();
				for (i=0; i<neighs.size(); i++){
					child = neighs.get(i);
					if (!(child.getVis())) {
						dist = child.getDist(current);
						if (dist < child.getG()) {
							openSet.remove(child);
							child.setGF(current);
							openSet.add(child);
						}
					}
					if (openSet.size()>BOUND) openSet.pollLast();
				}
					if(openSet.size()>length[id])length[id]=openSet.size();
			}
		} catch (Exception e) {
            e.printStackTrace();
		}
		return null;
	}
	
	public static void kml(person C, person t, node closest, node cur, int BOUND, int NoT){
		try{
			Stack<String> path = new Stack<String>();
		
			path.push(C.getCoord());
			while (!cur.equals(closest)) {
				path.push(cur.getCoord());
				cur = cur.father;
			}
			path.push(closest.getCoord());
			path.push(t.getCoord());

			BufferedWriter output = new BufferedWriter(new FileWriter(new File("BOUND"+ BOUND +"_T="+ t.getId() +".out")));
			while(!path.isEmpty()) output.write(path.pop() + "\n");
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void clearNodes(HashMap<String, node> N) {
		node current;
		int i;
		
		for (HashMap.Entry<String, node> entry : N.entrySet()) {
			entry.getValue().clear();
		}
	}
}

class person{
    double x, y;
	int id;
    
    public person (double i, double j, int k) {
        x = i;
        y = j;
		id = k;
    }
    public double getx() {return x;}
    public double gety() {return y;}
	public int getId() {return id;}
	public String getCoord() {return x + "," + y + ",0";}
}

class node implements Comparable<node>{
    double x, y;
	public ArrayList<node> neighbours = new ArrayList<node>();
	node father;
	double g, h, f;
    boolean visited;
	
    public node(double i, double j, double x1, double y1) {
        x = i;
        y = j;
		g = Double.MAX_VALUE;
		h = distFrom(x1, y1);
		visited = false;
		father = null;
    }
	public double getG() {return g;}
	public double getH() {return h;}
	public ArrayList<node> getNeigh() {return neighbours;}
	public node getFather() {return father;}
	public String getKey() {return String.valueOf(x) + " " + String.valueOf(y);}
	public boolean getVis() {return visited;}
	public String getCoord() {return x + "," + y + ",0";}
	public double getDist(node n) {
		return n.g + distFrom(n.x, n.y);
	}
	
	public void setVis() {visited = true;}
	public void setGF(node n) {
		if (n != null) {
			father = n;
			g = n.g + distFrom(n.x, n.y);		
			f = g + h;
		} else g = 0;
    }
	public void clear() {
		g = Double.MAX_VALUE;
		visited = false;
		father = null;
	}

	public int compareTo(node n) {return Double.compare(f, n.f);}
	public boolean equals(node n) {
		boolean a= (x == n.x && y == n.y);
		//System.out.println(a);
		return a;
	}

	public double distFrom(double lng1, double lat1) {		/*https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java*/
		double earthRadius = 6371000; //meters
		double dLat = Math.toRadians(lat1-y);
		double dLng = Math.toRadians(lng1-x);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(y)) *
				   Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
    }
}
