package eip;

import java.util.Scanner;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.BasicConfigurator;

public class ProducerConsumer {
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		
		CamelContext context = new DefaultCamelContext();
		
		RouteBuilder routeBuilder = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("direct:consumer-1").to("log:affiche-1-log");
				from("direct:consumer-2").to("file:messages");
			}
		};
		
		RouteBuilder routeBuilder2 = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("direct:consumer-all")
					.choice()
						.when(header("entete1").isEqualTo("écrire"))
							.to("direct:consumer-2")
						.otherwise()
							.to("direct:consumer-1");
			}
		};
		
		routeBuilder.addRoutesToCamelContext(context);
		routeBuilder2.addRoutesToCamelContext(context);
		
		context.start();
		
		ProducerTemplate pt = context.createProducerTemplate();
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Entrez votre message : \n");
		while(sc.hasNext()) {
			String s1 = sc.next();
			if(s1.equals("exit")) {
				System.out.println("Vous quittez le programme ...\n");
				break;
			}
			
			if (s1.charAt(0) == 'w') {
				pt.sendBodyAndHeader("direct:consumer-all", s1.substring(1, s1.length()), "entete1", "écrire");
			} else {
				pt.sendBody("direct:consumer-all",s1);
			}
			System.out.println("Entrez votre message : \n");
		}
	}
}
