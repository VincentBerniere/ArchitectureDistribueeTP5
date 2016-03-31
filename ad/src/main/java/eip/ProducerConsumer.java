package eip;

import java.util.Scanner;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class ProducerConsumer {
	
	private static void printMenu(){
		System.out.println("Entrez votre message :");
		System.out.println("\t- all : Retourne toutes les villes");
		System.out.println("\t- search [nomVille] : Retourne toutes les villes avec comme nom [nomVille]");
		System.out.println("\t- geonames [nomVille] : Retourne toutes les villes via Geonames avec comme nom [nomVille]");
		System.out.println("\t- exit : Stop le programme");
	}
	
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
		
		// Route qui retourne toutes les villes
		RouteBuilder routeGetAll = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:CitymanagerGetAll")
					.setHeader(Exchange.HTTP_METHOD,constant("GET"))
					.to("http://127.0.0.1:8084/all")
					.log("reponse received : ${body}");
			}
		};
		
		// Route qui retourne une ville passée en paramètre
		RouteBuilder routeGetCityByName = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:CitymanagerGetCityByName")
					.setHeader(Exchange.HTTP_METHOD,constant("GET"))
					.recipientList(simple("http://127.0.0.1:8084/" + "${body}"))
					.log(LoggingLevel.INFO,"reponse received : ${body}");
			}
		};
		
		// Route qui retourne une ville passée en paramètre (avec le service Geonames)
		RouteBuilder routeGetCityByNameGeonames = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:CitymanagerGetCityByNameGeonames")
					.setHeader(Exchange.HTTP_METHOD,constant("GET"))
					.recipientList(simple("http://api.geonames.org/search?username=m1gil&name_equals=" + "${body}"))
					.log(LoggingLevel.INFO,"reponse received : ${body}");
			}
		};
		
		RouteBuilder routeJgroups = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("direct:consumer-3").to("jgroups:m1gil");
				from("jgroups:m1gil").log("reponse received : ${body}");
			}
		};
		
		routeBuilder.addRoutesToCamelContext(context);
		routeBuilder2.addRoutesToCamelContext(context);
		routeGetAll.addRoutesToCamelContext(context);
		routeGetCityByName.addRoutesToCamelContext(context);
		routeGetCityByNameGeonames.addRoutesToCamelContext(context);
		routeJgroups.addRoutesToCamelContext(context);

		context.start();
		
		ProducerTemplate pt = context.createProducerTemplate();
		
	
		printMenu();
		
		Scanner sc = new Scanner(System.in);
		while(sc.hasNext()) {
			String s1 = sc.nextLine();
			if(s1.equals("exit")) {
				System.out.println("Vous quittez le programme ...\n");
				break;
			}
			

			if (s1.charAt(0) == 'w') {
				pt.sendBodyAndHeader("direct:consumer-all", s1.substring(1, s1.length()), "entete1", "écrire");
			} else if(s1.equals("all")){
				pt.sendBody("direct:CitymanagerGetAll",null);
			} else if(s1.contains("search")){
				s1 = s1.replace("search ","");
				System.out.println("Ville : " + s1);
				pt.sendBody("direct:CitymanagerGetCityByName",s1);
			} else if(s1.contains("geonames")){
				s1 = s1.replace("geonames ","");
				System.out.println("Ville : " + s1);
				pt.sendBody("direct:CitymanagerGetCityByNameGeonames",s1);
			} else if(s1.contains("jgroups")){
				s1 = s1.replace("jgroups ","");
				pt.sendBody("direct:consumer-3",s1);
			} 
				
			printMenu();
		}
	}
}
