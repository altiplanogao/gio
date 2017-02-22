package being.altiplano.ioservice;

import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;
import being.altiplano.ioservice.bio.BioServer;
import net.moznion.random.string.RandomStringGenerator;
import org.junit.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by gaoyuan on 22/02/2017.
 */
public class SocketServerClientTestBase {
    static final RandomStringGenerator generator = new RandomStringGenerator();
    static final String stringRegex = "\\w+\\d*\\s[0-9]{0,3}X";
    static final Method[] checkMethods;
    static final Random random = new Random();

    static {
        List<Method> lm = new ArrayList<>();
        Method[] allMethods = SocketServerClientTestBase.class.getDeclaredMethods();
        for (Method m : allMethods) {
            if (m.getName().startsWith("check") && (!m.getName().contains("Start")) && (!m.getName().contains("Stop"))) {
                Class<?>[] paramTypes = m.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[0].equals(IClient.class)) {
                    lm.add(m);
                }
            }
        }
        checkMethods = lm.toArray(new Method[lm.size()]);
    }

    protected void checkStart(IClient client) throws IOException {
        StartCommand.Config startConf = new StartCommand.Config();
        startConf.lag = 20;
        StartReply reply = client.call(new StartCommand(startConf));
        Assert.assertNotNull(reply);
    }

    protected void checkStop(IClient client) throws IOException {
        StopReply reply = client.call(new StopCommand());
        Assert.assertNotNull(reply);
    }

    protected void checkLowerCast(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkLowerCast(client, contentReference);
    }

    protected void checkLowerCast(IClient client, String contentReference) throws IOException {
        LowerCastReply reply = client.call(new LowerCastCommand(contentReference));
        Assert.assertNotNull(reply);
        Assert.assertEquals(reply.getContent(), contentReference.toLowerCase());
    }

    protected void checkUpperCast(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkUpperCast(client, contentReference);
    }

    protected void checkUpperCast(IClient client, String contentReference) throws IOException {
        UpperCastReply reply = client.call(new UpperCastCommand(contentReference));
        Assert.assertNotNull(reply);
        Assert.assertEquals(reply.getContent(), contentReference.toUpperCase());
    }

    protected void checkReverse(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkReverse(client, contentReference);
    }

    protected void checkReverse(IClient client, String contentReference) throws IOException {
        ReverseReply reply = client.call(new ReverseCommand(contentReference));
        Assert.assertNotNull(reply);
        Assert.assertEquals(reply.getContent(), new StringBuilder().append(contentReference).reverse().toString());
    }

    protected void checkCount(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkCount(client, contentReference);
    }

    protected void checkCount(IClient client, String contentReference) throws IOException {
        CountReply reply = client.call(new CountCommand(contentReference));
        Assert.assertNotNull(reply);
        Assert.assertEquals(reply.getCount(), contentReference.length());
    }

    protected void checkEcho(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        int times = 1 + random.nextInt(5);
        checkEcho(client, contentReference, times);
    }

    protected void checkEcho(IClient client, String contentReference, int times) throws IOException {
        EchoReply reply = client.call(new EchoCommand(times, contentReference));
        Assert.assertNotNull(reply);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            sb.append(contentReference);
        }
        Assert.assertEquals(reply.getContent(), sb.toString());
    }

    protected void checkRandom(IClient client, int times) throws IOException {
        int ms = checkMethods.length;
        try {
            for (int i = 0; i < times; ++i) {
                Method m = checkMethods[random.nextInt(ms)];
                m.invoke(this, client);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assert.fail(e.getMessage());
        }
    }


    protected void closeSocketClient(IClient client) throws IOException {
        checkStop(client);
        client.disConnect();
    }

    protected IClient createSocketClient(Class<? extends IClient> clientClz) throws IOException {
        IClient client = null;
        try {
            client = clientClz.getConstructor(int.class).newInstance(TestConfig.PORT);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        client.connect();
        checkStart(client);
        return client;
    }

    protected IServer createSocketServer(Class<? extends IServer> serverClz) throws IOException {
        IServer server = null;
        try {
            server = serverClz.getConstructor(int.class).newInstance(TestConfig.PORT);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        return server;
    }
}
