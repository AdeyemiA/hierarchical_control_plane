import java.io.IOException;


public class start {

	public static void main(String[] args) throws IOException {
		//main access
		if(args.length==1){
			ControllerNode cn = new ControllerNode(args[0]);
		}else if(args.length==0){
			ControllerNode cn = new ControllerNode();
		}else{
			System.out.println("Wrong parameters!");
		}
	}

}
