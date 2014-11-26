package android.learning.com.gesturetest;

import android.app.Instrumentation;
import android.graphics.Point;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.MotionEvent;
import static android.view.MotionEvent.*;
import android.view.View;
import android.widget.TextView;

/**
 * Created by noval on 10/13/14.
 */
public class MyActivityTest extends ActivityInstrumentationTestCase2<MyActivity> {
    private static final int EVENT_MIN_INTERVAL = 2;
    private MyActivity myActivity;
    private View rootView;
    private TextView textView;

    public MyActivityTest() {
        super(MyActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myActivity = getActivity();
        rootView = myActivity.getWindow().getDecorView();
        textView = (TextView) myActivity.findViewById(R.id.text_hello_world);
    }

    public void testPreconditions() {
        assertNotNull("instance is nul", myActivity);
        assertNotNull("text view is null", textView);
    }

    public void testTextViewOnScreen() {
        ViewAsserts.assertOnScreen(rootView, textView);
    }

    public void testTextViewShouldStartWithHelloWordText() {
        String expectedResult = "Hello world!";

        assertEquals(expectedResult, textView.getText().toString());
    }

    public void testTouchScreenShouldChangeTextWithJajal() {
        View view = myActivity.getWindow().getDecorView();

        TouchUtils.clickView(this, view);

        assertEquals("Jajal", textView.getText().toString());
    }

    public void testPinchOnRootViewShouldChangeTextWithPinchout() {
        String expectedResult = "Pinchout";
        generateZoomGesture(getInstrumentation(),3l, true, new Point(0,0),new Point(0,0), new Point(200,200), new Point(200,200), 3 );
        assertEquals(expectedResult, textView.getText().toString());
    }

    public static void generateZoomGesture(Instrumentation inst,
                                           long startTime, boolean ifMove, Point startPoint1,
                                           Point startPoint2, Point endPoint1,
                                           Point endPoint2, int duration) {

        if (inst == null || startPoint1 == null
                || (ifMove && endPoint1 == null)) {
            return;
        }

        long eventTime = startTime;
        long downTime = startTime;
        MotionEvent event;
        float eventX1, eventY1, eventX2, eventY2;

        eventX1 = startPoint1.x;
        eventY1 = startPoint1.y;
        eventX2 = startPoint2.x;
        eventY2 = startPoint2.y;

        // specify the property for the two touch points
        PointerProperties[] properties = new PointerProperties[2];
        PointerProperties pp1 = new PointerProperties();
        pp1.id = 0;
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
        PointerProperties pp2 = new PointerProperties();
        pp2.id = 1;
        pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;

        properties[0] = pp1;
        properties[1] = pp2;

        //specify the coordinations of the two touch points
        //NOTE: you MUST set the pressure and size value, or it doesn't work
        PointerCoords[] pointerCoords = new PointerCoords[2];
        PointerCoords pc1 = new PointerCoords();
        pc1.x = eventX1;
        pc1.y = eventY1;
        pc1.pressure = 1;
        pc1.size = 1;
        MotionEvent.PointerCoords pc2 = new MotionEvent.PointerCoords();
        pc2.x = eventX2;
        pc2.y = eventY2;
        pc2.pressure = 1;
        pc2.size = 1;
        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        //////////////////////////////////////////////////////////////
        // events sequence of zoom gesture
        // 1. send ACTION_DOWN event of one start point
        // 2. send ACTION_POINTER_2_DOWN of two start points
        // 3. send ACTION_MOVE of two middle points
        // 4. repeat step 3 with updated middle points (x,y),
        //      until reach the end points
        // 5. send ACTION_POINTER_2_UP of two end points
        // 6. send ACTION_UP of one end point
        //////////////////////////////////////////////////////////////

        // step 1
        event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, 1, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

        inst.sendPointerSync(event);

        //step 2
        event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_POINTER_INDEX_MASK, 2,
                properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

        inst.sendPointerSync(event);

        //step 3, 4
        if (ifMove) {
            int moveEventNumber = 1;
            moveEventNumber = duration / EVENT_MIN_INTERVAL;

            float stepX1, stepY1, stepX2, stepY2;

            stepX1 = (endPoint1.x - startPoint1.x) / moveEventNumber;
            stepY1 = (endPoint1.y - startPoint1.y) / moveEventNumber;
            stepX2 = (endPoint2.x - startPoint2.x) / moveEventNumber;
            stepY2 = (endPoint2.y - startPoint2.y) / moveEventNumber;

            for (int i = 0; i < moveEventNumber; i++) {
                // update the move events
                eventTime += EVENT_MIN_INTERVAL;
                eventX1 += stepX1;
                eventY1 += stepY1;
                eventX2 += stepX2;
                eventY2 += stepY2;

                pc1.x = eventX1;
                pc1.y = eventY1;
                pc2.x = eventX2;
                pc2.y = eventY2;

                pointerCoords[0] = pc1;
                pointerCoords[1] = pc2;

                event = MotionEvent.obtain(downTime, eventTime,
                        MotionEvent.ACTION_MOVE, 2, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

                inst.sendPointerSync(event);
            }
        }

        //step 5
        pc1.x = endPoint1.x;
        pc1.y = endPoint1.y;
        pc2.x = endPoint2.x;
        pc2.y = endPoint2.y;
        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        eventTime += EVENT_MIN_INTERVAL;
        event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_POINTER_INDEX_MASK, 2, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        inst.sendPointerSync(event);

        // step 6
        eventTime += EVENT_MIN_INTERVAL;
        event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, 1, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        inst.sendPointerSync(event);
    }
}
