package com.example.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouterExample extends RouteBuilder {

	@Autowired
	CamelContext context;
		
    @Override
    public void configure() {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);
       
        rest().post("{endPoint}/{urlName}").to("direct:restCall");
        
        rest().put("{endPoint}/{urlName}").to("direct:restCall");
        
        rest().delete("{endPoint}/{urlName}").to("direct:restCall");
        
        rest().get("{endPoint}/{urlName}").to("direct:restCall");
        
       // rest().post("soapCall").to("direct:soapCall");
        rest().post("soapCall").consumes("application/json").produces("application/json").type(Object.class)
  		.to("direct:soapCall");
        
      //Used for XSLT Transformation
      	Map<String, String> xmlJsonOptions = new HashMap<String, String>();
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
    	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "Root");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.TRIM_SPACES,"false");
      	xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_WHITESPACE,"true");

     
      //Get the service Endpoint using property files
      		PropertiesComponent pc = new PropertiesComponent();
      		pc.setLocation("classpath:endPoints.properties");
      		pc.setCache(false);
      		context.addComponent("properties", pc);
      		
      		onException(HttpOperationFailedException.class)
      		.onWhen(exchange -> {
      			HttpOperationFailedException
      			exe = exchange.getException(HttpOperationFailedException.class);
      			exchange.getIn().setBody(exe.getResponseBody());
      			return 417 == exe.getStatusCode();
      		})
      		.log("HTTP exception handled")
      		.handled(true)
      		.log("Error Occured :: "+"${body}") 
      		.to("direct:exception1");

      //Rest service call implementation with XSLT transform , call SOAP service with user input
      		from("direct:soapCall")
    		.marshal().json(JsonLibrary.Jackson)
    		.unmarshal().xmljson().to("log:INFO?showBody=true&showHeaders=true")
    		.to("xslt:file:XSLT/myTransform.xsl").to("log:INFO?showBody=true&showHeaders=true")
    		.removeHeaders("CamelHttp*")
    		.log("The Soap RequestBody :: "+"${body}")
    		.setBody(simple("${body}")).to("log:INFO?showBody=true&showHeaders=true")
    		.to("{{soapUrl}}").to("log:INFO?showBody=true&showHeaders=true").to("direct:response");
    		
    		from("direct:response")
    		.process(exchange -> {
    			String bodyValue=exchange.getIn().getBody(String.class);
    			exchange.getOut().setBody(bodyValue);
    		});
    		 
        //Rest service call implementation with xPath routing
		from("direct:restCall")
		.marshal().json(JsonLibrary.Jackson)
		.removeHeaders("CamelHttp*")
		.toD("properties:{{${header.endPoint}}}"+"${header.urlName}")
		.log("${body}")
		.streamCaching()
		.to("log:INFO?showBody=true&showHeaders=true")
		.to("direct:restCallRes");
		
       
		//Rest service call implementation
		from("direct:restCallRes")
		.process(exchange -> {
			String value =exchange.getIn().getBody(String.class);
			JSONParser parsers = new JSONParser(); 
			JSONObject resJsons = (JSONObject) parsers.parse(value.toString());
			System.out.println(" Response : "+resJsons);
			exchange.getIn().setBody(resJsons);
		});
		
		from("direct:exception1")
		.process(exchange -> {
			String rslt= exchange.getIn().getBody(String.class);
			System.out.println("The failure response :: " +rslt);
			JSONParser parsers = new JSONParser(); 
			JSONObject resJsons = (JSONObject) parsers.parse(rslt.toString());
			
			exchange.getOut().setBody(resJsons);
		});

    }
}
