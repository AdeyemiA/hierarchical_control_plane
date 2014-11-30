import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
	public static void main(String[] argv) throws NumberFormatException, UnknownHostException, IOException{
		
		Socket socket = new Socket(argv[0], Integer.parseInt(argv[1]));
		PrintWriter pw = new PrintWriter(socket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println("#####################");
		System.out.println("#     HCP Client    #");
		System.out.println("#####################");
		System.out.print(">");
		Scanner scanner = new Scanner(System.in);
		String line = "";
		while(!(line=scanner.nextLine()).equals("exit")){
			pw.println(line);
			pw.flush();
			if(line.equals("shutdown"))
				break;
			System.out.println(br.readLine());
			System.out.print(">");
		}
		
		pw.close();
		br.close();
		socket.close();
		scanner.close();
	}
}
