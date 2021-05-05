# Camel REST DSL OpenApi Maven Plugin

This is a custom Maven plugin, part of Tavros, for generating the REST DSL for projects generated using the OpenAPI Archetype. It is run automatically during the generation of the OpenAPI archetype.

According to the [HTTP 1.1 specification RFC7231](https://tools.ietf.org/html/rfc7231#section-4.3.1), certain HTTP methods do not have semantic definitions and therefore sending a payload body with such methods might cause some existing implementations to reject the request. OpenAPI 3.0.0 provides further guidance, specifying that a [requestBody SHALL be ignored by consumers](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#fixed-fields-8) for these methods. This plugin will still generate code according to the OpenAPI document, but runtime support will depend on the HTTP server library used and all other components on the request path, such as proxies and gateways.

There is one goal associated with this plugin:

`generate` - To generate the REST DSL for both RoutesGenerated and RoutesImplementation classes.

### How do I install the plugin locally? ###

Clone the main branch of this repo, change to the plugin directory, and run `mvn clean install`.

If you add or update a file and receive an error about missing headers, such as `Missing header in: `, run `mvn com.mycila:license-maven-plugin:format`. If you get an error with that command saying that you don't have the plugin locally, you can get it by running `mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.mycila:license-maven-plugin:4.0.rc2`.

### Debugging during development

If unfamiliar with debugging a maven plugin:

In the terminal, run `mvnDebug com.ms3-inc.tavros:camel-restdsl-openapi-plugin:<version>:generate -DspecificationUri=<testSpecUri>`

Then, in your IDE, create a configuration file with remote jvm debug, with the port that is outputted from the above command.

### Who do I talk to? ###

Contact:

* Rob Ratcliffe, rratcliffe@ms3-inc.com
