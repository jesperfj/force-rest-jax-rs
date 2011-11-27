# Sample JAX-RS Force.com App

This app is a sample of a JAX-RS Jersey/Grizzly app with Force.com OAuth authentication.

## Unpublished Dependencies

It currently has a dependency on the unpublished [Force.com REST API module](https://github.com/jesperfj/force-rest-api). So you need to build this locally first. It's added as a submodule, so all you have to do is clone this repo with --recursive:

    $ git clone --recursive [this-repo-url]

Then build the force-rest-api submodule manually:

    $ mvn -f force-rest-api/pom.xml -DskipTests install

(execute from the dir cloned from this repo)

## Build and Run

Once you have force-rest-api built in your local maven cache you can build this project:

    $ mvn package

## Configure OAuth

The sample will read OAuth credentials from the environment variables `CLIENT_ID` and `CLIENT_SECRET`. To obtain these values, create an OAuth Remote Access entry (an OAuth consumer) in your Force.com developer org and set `CLIENT_ID` to the consumer key value and `CLIENT_SECRET` to the consumer secret value.

If you are using the foreman tool to run your app locally, then you can store these values in a `.env` file. See the `env.sample` file for an example.

## Run the app

The Maven build script generates an execution wrapper in `target/bin/webapp`. If you have set your environment variables correctly you can now start it with:

    $ sh target/bin/webapp

If you're using foreman, you can start it with

    $ foreman start

## Deploying to Heroku

TODO. It may work, but detecting redirect URI automatically is still unresolved

