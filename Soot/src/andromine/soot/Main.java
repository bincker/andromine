package andromine.soot;

import soot.Body;
import soot.PackManager;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.SceneTransformer;
import soot.Scene;
import soot.Unit;
import soot.JastAddJ.ContinueStmt;
import soot.jimple.Stmt;

import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.Statement;

@SuppressWarnings("unused")
public class Main {

	static Hashtable<String, String> dict = new Hashtable<String, String>();
	static Hashtable<String, String> dict_classes = new Hashtable<String, String>();
	static Hashtable<String, Integer> apis = new Hashtable<String, Integer>();

	static String apkHash;
	static String apkName;
	static boolean verbose = false;

	public static boolean methodInClass(SootClass sc, String method){
		if (sc == null)
			return false;

		String key = sc.getName()+"."+method;
		if (dict.get(key) != null) // the method is part of this class
			return true;

		if (dict_classes.get(sc.getName()) != null) // the class was already visited
			return false;
		dict_classes.put(sc.getName(), sc.getName());

		for (int i =0; i < sc.getMethods().size(); i++) {
			SootMethod sm = sc.getMethods().get(i);
			String k = sc.getName()+"."+sm.getName();
			dict.put(k, sc.getName());
		}

		if (dict.get(key) != null)
			return true;
		return false;
	}


	public static void main(String[] args) {
		apkName = args[1].split(":")[0].replaceAll(" " , "");
		File file = new File(apkName);
		apkName = file.getAbsolutePath();
		System.err.println(apkName);
		// We get a connection to initialize database tables
		try {
			Connection conn = null;
			Statement stmt1 = null;

			Class.forName("com.mysql.jdbc.Driver");
			//conn =  DriverManager.getConnection("jdbc:mysql://localhost:3306/snt", "snt", "");
			conn =  DriverManager.getConnection("jdbc:mysql://localhost:3306/tegawende", "tegawende", "tegawende");

			stmt1 = (Statement) conn.createStatement();

			String sql1 = "CREATE TABLE IF NOT EXISTS apksPermissions" +
					"(apkHash VARCHAR(255) not NULL, apkName VARCHAR(255), permission VARCHAR(255), CONSTRAINT Perm PRIMARY KEY (apkHash, permission))";

			String sql2 = "CREATE TABLE IF NOT EXISTS apksMethodCalls" +
					"(apkHash VARCHAR(255) not NULL, apkName VARCHAR(255), method_name VARCHAR(255), class_name VARCHAR(255), package_name VARCHAR(255), occurences INTEGER, CONSTRAINT meth PRIMARY KEY (apkHash, method_name))";

			stmt1.executeUpdate(sql1);
			stmt1.executeUpdate(sql2);
			conn.close();
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// Get the permissions and compute apkHash using Androguard
		String pythonScriptPath = "permissions.py";
		//String pythonScriptPath = "/android/permissions.py";
		String[] cmd = new String[4];
		cmd[0] = "python";
		cmd[1] = pythonScriptPath;
		cmd[2] = "-i";
		cmd[3] = apkName;
		//System.err.println("CMD: " +cmd[0]+ " " + cmd[1]);
		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr;
		try {
			pr = rt.exec(cmd);
			pr.waitFor();
			// retrieve output from python script
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";

			//System.err.println("Hashval: " + apkHash);
			
			//while((line = bfr.readLine()) != null) {
			while(bfr.ready()) {
				line = bfr.readLine();
				apkHash = line;
				//System.err.println("LINE -- " + line);
			}
			//System.err.println("APK Hash value: " + apkHash);
			int res = pr.exitValue();
			if (res == 0)
				System.err.println("\tSuccessfully retrieved Permissions..." );
			else{
				System.err.println("\tFailed to execute Python script... : ("+res+")");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myTransform", new SceneTransformer() {
					@Override
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

									for (Unit u:sm.retrieveActiveBody().getUnits())
									{ 
										Stmt s = (Stmt)u;
										if (s.containsInvokeExpr()) {
											String method = s.getInvokeExpr().getMethodRef().name();
											SootClass mclass = s.getInvokeExpr().getMethodRef().resolve().getDeclaringClass();

											SootClass otherclass;
											if(mclass.getName().compareTo(sc.getName()) == 0 && methodInClass(mclass, method)){
												continue; // We don't care about internally-defined methods...
											}else if (mclass.getName().compareTo(sc.getName()) == 0){ // found an API
												otherclass = mclass.getSuperclass();
											}else {
												otherclass = mclass;
											}

											int iter= 0;
											boolean found = true;

											while(!methodInClass(otherclass, method)){
												if(otherclass == null){
													found = false;
													break;
												}
												otherclass = otherclass.getSuperclass();
												iter+=1;
												if (++iter > 10){ // I don't think we could go deep to that level :)
													found = false;
													break;
												}
											}

											if (found){ // Add it to a dictionnary 

												String f = otherclass.getName() + "/" + otherclass.getName() + "." + method + "/" + otherclass.getPackageName();
												Integer v = null;
												v = apis.get(f);
												if (v != null)
													apis.put(f, new Integer(v.intValue()+1));
												else
													apis.put(f, new Integer(1));
											}


										}

									}
								}
							}
						}

						// We get a connection to insert our results...
						// But I suppose this is for the end 
						try {
							Connection conn = null;
							Statement stmt = null;

							Class.forName("com.mysql.jdbc.Driver");
							//conn =  DriverManager.getConnection("jdbc:mysql://localhost:3306/snt", "snt", "");
							conn =  DriverManager.getConnection("jdbc:mysql://localhost:3306/tegawende", "tegawende", "tegawende");

							Enumeration<String> ks = apis.keys();
							while (ks.hasMoreElements()){
								String f = ks.nextElement();
								String[] sf = f.split("/");
								String clname = sf[0];
								String mthname = sf[1];
								String packname = sf[2];
								int occurences = apis.get(f);

								stmt = (Statement) conn.createStatement();

								String sql = "INSERT INTO apksMethodCalls" +
										"(apkHash, apkName, method_name, class_name, package_name, occurences) VALUES ('"+ 
										apkHash
										+"', '" + apkName
										+"', '" + mthname 
										+"', '" + clname
										+"', '" + packname
										+"', " + occurences
										+")";

								stmt.executeUpdate(sql);
							}
							conn.close();
						} catch (SQLException ex) {
							System.err.println("\tSQLException: " + ex.getMessage());
							System.err.println("\t\t" + ex.getLocalizedMessage());

						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return;
						}
					}
				}));
		soot.Main.main(args);
	}
}
