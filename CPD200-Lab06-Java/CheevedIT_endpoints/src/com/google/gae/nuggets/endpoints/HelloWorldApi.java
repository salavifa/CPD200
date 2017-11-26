package com.google.gae.nuggets.endpoints;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiResourceProperty;

import java.io.IOException;

import javax.inject.Named;

@Api(name = "helloworldendpoints",
version = "v1",
namespace = @ApiNamespace(ownerDomain = "nuggets.gae.google.com",
    ownerName = "nuggets.gae.google.com",
    packagePath = ""))
public class HelloWorldApi {

	  @ApiMethod(name = "sayHello")
	  public Hello say_hello() {
	    Hello response = new Hello();
	    response.setData("Hello world");

	    return response;
	  }
	  
	  @ApiMethod(name = "sayHelloByName")
	  public Hello say_hellobyname(@Named("name") String name) {
	    Hello response = new Hello();
	    response.setData("Hello " + name);

	    return response;
	  }  
	  
}
