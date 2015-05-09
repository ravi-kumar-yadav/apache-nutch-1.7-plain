package org.apache.nutch.crawl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.hadoop.conf.Configuration;

public class Classifier{
	String trainedModel;
	Process p;
	Configuration conf;
	public static int serverPort;

	
	// classifier_server_port --> 9876
	public Classifier(Configuration c){
		trainedModel="";
		conf=c;
		serverPort = Integer.parseInt(conf.get("classifier_server_port"));
	}
	
	
	//** Train Classifier from train.txt(inpFile)
	//** Command Used :: svm_learn -I train model
	//** FUTURE, We can use incremental training approach :: svm_learn -M model train model2
	public int learn(String inpFile){
		try{
			//../src/svm_learn  train2.svmdata model
			String svmPath=conf.get("TinySVMClassifier");
			String modelFilePath = conf.get("ClassifierModelPath");
			
			//										svm_learn -I	train 			model
			p = Runtime.getRuntime().exec(svmPath+"/svm_learn -I "+inpFile+" "+modelFilePath+"/model");
	  		p.waitFor();
	  	}
	  	catch(Exception e){
	  		e.printStackTrace();
	  		return -1;
	  	}

	  	return 1;
	}


	// It takes old model file(model_temp) and new train file(incTrain.txt) and create new model(model)
	public int incLearn(String inpFile){
		try{
			// cat model > model_temp
			// ../src/svm_learn -I -M model_temp inpFile model
			String svmPath=conf.get("TinySVMClassifier");
			String modelFilePath = conf.get("ClassifierModelPath");
			p = Runtime.getRuntime().exec("cat "+modelFilePath+"/model > "+modelFilePath+"/model_temp");
			p.waitFor();
												//	svm_learn -I -M 	model_temp 					inpFile 		model
			p = Runtime.getRuntime().exec(svmPath+"/svm_learn -I -M "+modelFilePath+"/model_temp "+inpFile+" "+modelFilePath+"/model");
	  		p.waitFor();
	  		p = Runtime.getRuntime().exec("rm -f "+modelFilePath+"/model_temp");
	  		p.waitFor();
	  	}
	  	catch(Exception e){
	  		e.printStackTrace();
	  		return -1;
	  	}

	  	return 1;
	}
	
	
	// It start a Classifier at a given port
	public void startClassifier(){
		try{
			//			
			String svmPath = conf.get("TinySVMClassifier");
			String modelFilePath = conf.get("ClassifierModelPath");
			
			//										svm_classify -V test.svmdata model
			p = Runtime.getRuntime().exec(svmPath+"/svm_classify "+modelFilePath+"/model " + (++serverPort));
//	  		p.waitFor();
			System.out.println("port = "+serverPort);
	  	}catch(Exception e){
	  		e.printStackTrace();	
	  	}
	}
	
	public void stopClassifier(){
		try{
			//
			p.destroy();
			System.out.println("status of p="+p.exitValue());
//	  		p.waitFor();
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	}
	
	
	// It creates an "client" socket object that listens to the port and serves the request 
	public float classify(String test){
		float score=(float) 0.00;
		try{
			System.out.println("classify port = "+serverPort);
			String serverIP = conf.get("classifier_server_ip");
			Socket client = null;
	        while(true) {
	        try {
	        	client = new Socket(serverIP, serverPort);
	        	System.out.println("Got through");
	        	break;
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Got exception : " +e.getMessage());
			}
	        }
	        System.out.println("Just connected to "
	                    + client.getRemoteSocketAddress());   

	        InputStream dis = client.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(dis));
	        System.out.println("Input stream has "+dis.available()+" bytes available");

	        try {
	            OutputStream out = client.getOutputStream();
	            PrintStream printStream = new PrintStream(out);
	            printStream.print(test);
	            out.flush();
	            
	            String response = reader.readLine();
	            //System.out.println("Inside Classifier : "+response);
	            if(response==null||"nan".equalsIgnoreCase(response)) {
	            	System.out.println("Got nan for :"+test);
	            	return -100;
	            }
	            score = Float.parseFloat(response);
	            
	            
	            printStream.close();
	            
	            client.close();

	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
	  	}
	  	catch(Exception e){
	  		e.printStackTrace();
	  		return -100;
	  	}

	  	return score;

	}

}