package vax.test.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Base64Coder;

public class TestGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture img;
    private final ConstantProvider constantProvider = new ConstantProvider();

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture( "badlogic.jpg" );
        //sendHttpLog( "log1.log", "test delay message" );
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor( 1, 0, 0, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
        batch.begin();
        batch.draw( img, 0, 0 );
        batch.end();
        throw new RuntimeException();
    }

    /**
     @param logName
     @param logMessage
     @return true if the log can be sent at all; false if HTTP logging is disabled
     */
    public boolean sendHttpLog( String logName, String logMessage ) {
        String httpLogUrlString = constantProvider.getHttpLogUrlString();
        if ( httpLogUrlString == null ) {
            return false;
        }
        /*
         Charset charset = StandardCharsets.UTF_8;
         String charsetName = charset.name();
         */
        Net.HttpRequest httpReq = new Net.HttpRequest( "POST" );
        String httpContent = "magic=" + constantProvider.getMagicNumber() + "&logName=" + logName + "&logData="
                + Base64Coder.encodeString( logMessage );
        /*
         try {
         httpReq.setContent( URLEncoder.encode( httpContent, charsetName ) );
         } catch (UnsupportedEncodingException ex) {
         MainLogger.critical( "URLEncoder failed due to '", ex, "'" ); // we can safely assume this won't happen at all
         } */
        httpReq.setContent( httpContent );
        httpReq.setUrl( httpLogUrlString );
        String contentType = "application/x-www-form-urlencoded";//+"; charset=" + charsetName;
        httpReq.setHeader( "Content-Type", contentType );
        //httpReq.setHeader( "Content-Length", Integer.toString( logMessage.length() ) ); // causes trouble on Android

        //final VaxHttp httpTest = new VaxHttp();
        final Net httpTest = Gdx.net;
        httpTest.sendHttpRequest( httpReq, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse( Net.HttpResponse httpResponse ) {
                int status = httpResponse.getStatus().getStatusCode();
                MainLogger.info( "received HTTP status '", status, "' in log send response (", status == 200 ? "OK" : "error", ")" );
            }

            @Override
            public void failed( Throwable t ) {
                MainLogger.warning( "HTTP request caused '", t, "'" );
            }

            @Override
            public void cancelled() {
                MainLogger.warning( "HTTP request cancelled" );
            }
        } );
        //httpTest.getExecutorService().shutdownNow();
        /*
         try {
         httpTest.getExecutorService().awaitTermination( 1, TimeUnit.SECONDS );
         } catch (InterruptedException ex) {
         throw new RuntimeException( ex );
         }
         */
        return true;
    }

    public static class MainLogger {
        private static void info( Object... params ) {
            for( Object o : params ) {
                System.out.print( o );
            }
            System.out.println();
        }

        private static void warning( Object... params ) {
            for( Object o : params ) {
                System.out.print( o );
            }
            System.out.println();
        }

        private MainLogger() {
        }
    }

    public class ConstantProvider {
        public String getAppName() {
            return "Test";
        }

        public int getMagicNumber() {
            return 8675309; // replace this with some "app id" number later on
        }

        public String getHttpLogUrlString() {
            return "http://vaxquis.cba.pl/logger.php"; // TODO provide a real logger target here later on
        }
    }

}
