# Frontend

[<img src="https://img.shields.io/travis/playframework/play-java-starter-example.svg"/>](https://travis-ci.org/playframework/play-java-starter-example)

# Data Science Infrastructure Lab Project -- Sci-Hub Frontend


## frontend Dependencies
### UI
- Materilize
### JavaScript
- Backbone.js 1.1.0
- JQuery
- Materialize
- D3.js
- Quill
- Underscore.js
- Parsley.js 2.9.2

## Running

Run this using [sbt](http://www.scala-sbt.org/).  If you downloaded this project from http://www.playframework.com/download then you'll find a prepackaged version of sbt in the wish directory:

```
sbt run
```

And then go to http://localhost:9000 to see the running web application.

## Controllers

There are several demonstration files available in this template.

- HomeController.java:

  Shows how to handle simple HTTP requests.

- AsyncController.java:

  Shows how to do asynchronous programming when handling a request.

- CountController.java:

  Shows how to inject a component into a controller and use the component when
  handling requests.

## Components

- Module.java:

  Shows how to use Guice to bind all the components needed by your application.

- Counter.java:

  An example of a component that contains state, in this case a simple counter.

- ApplicationTimer.java:

  An example of a component that starts when the application starts and stops
  when the application stops.

## Filters

- ExampleFilter.java

  A simple filter that adds a header to every response.

## Frontend form verification practice instruction
In this section we will not use external plugin for user input verification, html5 have API can cover most scenario.
For every `<input>` tag, we should assign specific type attributes e.g. `type="email"`
Please find documentation from [w3c](https://www.w3schools.com/html/html_form_input_types.asp)
add `required` for any required input.
Also, you should include `class="validate"` for materialize to help us validate and show message

For better user experience, you can add some error and sucess message in a following `<span></span>` element,
use attributes: `data-error="please enter a valid value here"` and `data-success="✓"` for dynamically validate users' input and remind them.

For any customized validation, you can include a regular expression in pattern attributes.
You can restrict length of user input by data-length attributes

Overall, an example for email input with only 55 characters email
```
<input type="email" name="email" id="email" class="validate" data-length="55" required onchange="checkValidEmail()">
<span id="emailValidationMsg" class="helper-text" data-error="Please enter a valid email address"  data-success="✓">
```
FYI, there is a constant variable for saving regular expression in `field_validation_helper.js` file.


