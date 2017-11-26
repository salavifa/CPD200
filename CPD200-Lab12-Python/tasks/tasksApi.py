import endpoints
from protorpc import messages
from protorpc import message_types
from protorpc import remote

# If the request contains path or querystring arguments,
# you cannot use a simple Message class.
# Instead, you must use a ResourceContainer class
REQUEST_CONTAINER = endpoints.ResourceContainer(
    message_types.VoidMessage,
    name=messages.StringField(1),
)


class Hello(messages.Message):
    """String that stores a message."""
    greeting = messages.StringField(1)


@endpoints.api(name='helloworldendpoints', version='v1')
class HelloWorldApi(remote.Service):
    """Helloworld API v1."""

    @endpoints.method(message_types.VoidMessage, Hello,
      path = "sayHello", http_method='GET', name = "sayHello")
    def say_hello(self, request):
      return Hello(greeting="Hello World")

    @endpoints.method(REQUEST_CONTAINER, Hello,
      path = "sayHelloByName", http_method='GET', name = "sayHelloByName")
    def say_hello_by_name(self, request):
      greet = "Hello {}".format(request.name)
      return Hello(greeting=greet)


EndpointsApp = endpoints.api_server([HelloWorldApi])