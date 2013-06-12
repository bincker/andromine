package andromine.soot;

import soot.Body;
import soot.PackManager;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.SceneTransformer;
import soot.Scene;
import soot.Unit;
import soot.jimple.Stmt;

import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;


public class Main {

	static Hashtable<String, String> dict = new Hashtable<String, String>();
	static Hashtable<String, String> dict_classes = new Hashtable<String, String>();

	static boolean verbose = false;

	public static boolean methodInClass(SootClass sc, String method){
		if (sc!= null && sc.getName().compareTo("android.app.Activity") == 0){
			sc.setResolvingLevel(10);
			System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXx " );
			System.err.println("We are in "+sc.getName()+ "("+sc.getMethodCount() + ")");
			for (SootMethod sm: sc.getMethods()) {
				System.err.println("\t\t"+sm.getName());
			}
			verbose = true;
		}
		String n;
		if (sc==null)
			n = "NULL";
		else
			n =  sc.getName();


		if (verbose)
			System.err.println("STEP 1. -- "+n);

		if (sc == null)
			return false;

		if (verbose)
			System.err.println("STEP 2. -- "+n);

		String key = sc.getName()+"."+method;
		if (dict.get(key) != null) // the method is part of this class
			return true;

		if (verbose)
			System.err.println("STEP 3. -- "+n);

		if (dict_classes.get(sc.getName()) != null){ // the class was already visited
			System.err.println("\tFALSE-- "+n);
			return false;
		}

		if (verbose)
			System.err.println("STEP 4. -- "+n);
		dict_classes.put(sc.getName(), sc.getName());

		if (verbose)
			System.err.println("STEP 5. -- "+n);
		for (int i =0; i < sc.getMethods().size(); i++) {
			SootMethod sm = sc.getMethods().get(i);
			String k = sc.getName()+"."+sm.getName();
			if (verbose){
				System.err.println("\t\t"+k);
				System.err.println("Number of methods ("+ sc.getName()+") :"+ sc.getMethodCount() + " and " + sc.getMethods().size());
			}
			dict.put(k, sc.getName());
		}

		if (verbose)
			System.err.println("STEP 6. -- "+n);
		//System.out.println("We are here now   -- " + dict.size());
		if (dict.get(key) != null){
			if (verbose){
				System.out.println("Should be in");
			}
			return true;
		}
		return false;
	}


	public static void main(String[] args) {
		System.out.println("Beginning of Main");
		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myTransform", new SceneTransformer() {
					protected void internalTransform(String phaseName, Map
							options) {

						for (SootClass sc: Scene.v().getApplicationClasses()) {

							for (SootMethod sm: sc.getMethods()) {
								if (!sm.isConcrete())
									continue;

								Body b = sm.retrieveActiveBody();
								if (b == null)
									continue;

								if (sm.hasActiveBody()){

									//System.err.println("@@@@@@@@@ "+sc.getName()+ "." +sm.getName());
									for (Unit u:sm.retrieveActiveBody().getUnits())
									{ //sm.getActiveBody().getUnits()) {
										Stmt s = (Stmt)u;
										if (s.containsInvokeExpr()) {
											String method = s.getInvokeExpr().getMethodRef().name();
											SootClass mclass = s.getInvokeExpr().getMethodRef().resolve().getDeclaringClass();

											if(methodInClass(mclass, method)){
												continue; // We don't care about internally-defined methods...
											}

											SootClass superclass = mclass.getSuperclass();
											int iter= 0;
											boolean found = true;
											verbose = false;
											/*if (superclass!= null && superclass.getName().compareTo("android.app.Activity") == 0){
												System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXx");
												verbose = true;
											}*/
											while(!methodInClass(superclass, method)){
												if(superclass == null){
													found = false;
													break;
												}
												System.err.println("\tsuperclass: '" + superclass +"'");

												superclass = superclass.getSuperclass();
												iter+=1;
												if (++iter > 10){ // I don't think we could go deep to that level :)
													found = false;
													break;
												}
											}

											if (!found){
												System.err.println("FAILED TO FIND CORRESPONDANCE OF '" + method +"'");
											}else{
												System.err.println("api: " +superclass.getPackageName()+"."+method);
											}

											/*System.err.println("----------------");
											System.err.println(s.getInvokeExpr());
											System.err.println("----------------");
											System.err.println("declaring class: "+
													s.getInvokeExpr().getMethodRef().declaringClass());
											System.err.println("++++++++++++++++");
											System.err.println("package name: "+
													s.getInvokeExpr().getMethodRef().declaringClass().getPackageName());
											System.err.println("++++++++++++++++");
											System.err.println("****************");
											System.err.println("Invoked Method: "+
													s.getInvokeExpr().getMethodRef().resolve().getDeclaringClass().getName());
											System.err.println("****************");*/

										}

									}
								}
							}
						}

						Enumeration<String> ks = dict.keys();
						while (ks.hasMoreElements()){
							System.out.println(ks.nextElement());
						}
					}
				}));
		soot.Main.main(args);
	}
}

/*public class Main {
	public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myTransform", new SceneTransformer() {
					protected void internalTransform(String phaseName, Map
							options) {
						System.out.println("My Transformer");
						//System.err.println(Scene.v().getApplicationClasses());
						for (SootClass sc: Scene.v().getApplicationClasses()) {
							System.out.println("class '"+ sc.getName() +"'");
							for (SootMethod sm: sc.getMethods()) {
								System.out.println("XXXXXXXXXXXXmethod: "+ sm);
								if (!sm.hasActiveBody()) {
									System.out.println("skipping method "+ sm);
									continue;
								}
								for (Unit u: sm.getActiveBody().getUnits()) {
									Stmt s = (Stmt)u;
									if (s.containsInvokeExpr())
										System.out.println(s.getInvokeExpr());                    
								}
							}
						}
					}
				}));
		soot.Main.main(args);
	}
}*/