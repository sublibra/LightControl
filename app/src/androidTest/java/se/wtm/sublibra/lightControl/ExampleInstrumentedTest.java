package se.wtm.sublibra.lightControl;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    ServerCommunication sc = null;

    @Before
    public void setUp() throws Exception {
        sc = new ServerCommunication();
    }

    @After
    public void tearDown() throws Exception {
        sc = null;
    }

    @Test
    public void testParseDeviceList() throws Exception {

        String devListJson =
                "{" +
                        "    \"device\": [" +
                        "    {" +
                        "        \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev1\"," +
                        "           \"parameter\": {"+
                        "               \"fade\": \"false\","+
                        "               \"house\": \"14032890\","+
                        "               \"code\": \"0000000000\","+
                        "               \"system\": \"1\","+
                        "               \"unit\": \"16\""+
                        "           },"+
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\"," +
                        "           \"id\": 1," +
                        "           \"clientName\": \"TellProx\"," +
                        "           \"methods\": 0" +
                        "   }," +
                        "   {" +
                        "       \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev2\"," +
                        "           \"parameter\": {"+
                        "               \"fade\": \"true\","+
                        "               \"house\": \"14032890\","+
                        "               \"code\": \"0000000000\","+
                        "               \"system\": \"1\","+
                        "               \"unit\": \"16\""+
                        "           },"+
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\"," +
                        "           \"id\": 2," +
                        "           \"clientName\": \"TellProx\"," +
                        "           \"methods\": 0" +
                        "   }" +
                        "   ]" +
                        "}";


        Device dev1 = new Device("Dev1", false, 1);
        Device dev2 = new Device("Dev2", true, 2);
        Device[] testDevArray = {dev1, dev2};
        Device[] retDevArray = sc.parseDeviceList(devListJson);
        assertArrayEquals(retDevArray, testDevArray);
    }

    @Test(expected = JSONException.class)
    public void testJSONParsingException() throws Exception {
        String brokenJSON =
                "{" +
                        "    \"device\": [" +
                        "    {" +
                        "        \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev1\"," +
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\"," +
                        "           \"id\": 1," +
                        "           \"clientName\": \"TellProx\"," +
                        "           \"methods\": 0" +
                        "   {" +
                        "       \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev2\"," +
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\",";



        // should throw JSONexception
        sc.parseDeviceList(brokenJSON);
    }

    @Test(expected = JSONException.class)
    public void testMissingStartNode() throws Exception {
        String noStartNode =
                "{" +
                        "    {" +
                        "        \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev1\"," +
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\"," +
                        "           \"id\": 1," +
                        "           \"clientName\": \"TellProx\"," +
                        "           \"methods\": 0" +
                        "   }," +
                        "   {" +
                        "       \"statevalue\": \"0\"," +
                        "           \"name\": \"Dev2\"," +
                        "           \"editable\": 1," +
                        "           \"state\": 2," +
                        "           \"client\": 1," +
                        "           \"online\": 1," +
                        "           \"type\": \"device\"," +
                        "           \"id\": 2," +
                        "           \"clientName\": \"TellProx\"," +
                        "           \"methods\": 0" +
                        "   }" +
                        "   ]" +
                        "}";
        sc.parseDeviceList(noStartNode);
    }

    @Test(expected = IOException.class)
    public void testNullInput() throws Exception {
        sc.parseDeviceList(null);
        sc.parseDeviceList("");
    }
}
