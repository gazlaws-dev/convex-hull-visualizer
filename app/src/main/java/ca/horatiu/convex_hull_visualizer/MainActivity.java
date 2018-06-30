package ca.horatiu.convex_hull_visualizer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

/**
 * This class is the starter activity, which reads gestures and uses the GridView class to show it.
 *
 * @author Horatiu Lazu
 */

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private int N=1,n=1;
    /**
     * mDetector GestureDetectorCompat This is the GestureDetector reference.
     */
    private GestureDetectorCompat mDetector;
    /**
     * scaleGestureDetector ScaleGestureDetector This is used to detect gestures.
     */
    ScaleGestureDetector scaleGestureDetector;
    /**
     * gridRenderer GridView This is the GridView reference.
     */
    GridView gridRenderer;
    /**
     * grid Grid This is the grid reference.
     */
    private Grid grid;
    /**
     * settings Settings This is the settings reference to store the settings.
     */
    private Settings settings;
    /**
     * points ArrayList This is the arraylist of points.
     */
    private ArrayList<Coordinate> points = new ArrayList<Coordinate>();
    /**
     * hull Hull This is the hull reference.
     */
    private static Hull hull; //ehh, seems a bit sketchy lol.

    /**
     * This method sets the grid to a specified value.
     *
     * @param grid Grid This is the Grid reference.
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * This method returns the grid.
     *
     * @return Settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * This method sets the starting x value.
     *
     * @param x int This is the starting value.
     */
    public void setStartX(int x) {
        settings.setXStart(x);
        refresh();
    }

    /**
     * This method sets the starting y value.
     *
     * @param y int This is the starting y value set.
     */
    public void setStartY(int y) {
        settings.setYStart(y);
        refresh();
    }

    /**
     * This method sets the skip value.
     *
     * @param skip int This is the new skip value.
     */
    public void setSkip(int skip) {
        settings.setSkip(skip);
        refresh();
    }

    /**
     * This method returns the hull.
     *
     * @return Hull This would be the hull.
     */
    public static Hull getHull() {
        return hull;
    }

    /**
     * This method sets up the graphics on the screen.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        settings = new Settings(Settings.DEFAULT_START_X, Settings.DEFAULT_START_Y, Settings.DEFAULT_SKIP);
        Coordinate.populateColorConverter();
        gridRenderer = new GridView(this, grid, settings);
        setContentView(R.layout.activity_main);

        LinearLayout upper = (LinearLayout) findViewById(R.id.LinearLayout01);
        upper.addView(gridRenderer);

        scaleGestureDetector = new ScaleGestureDetector(this, new MyOnScaleGestureListener(this));
        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

    }

    /**
     * This is the settings onClick call
     *
     * @param v
     */
    public void settings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * This method resets the grid.
     *
     * @param v
     */
    public void reset(View v) {
        if (points == null) //safety? no lol still crashes. odd..
            return;
        for (int x = 0; x < points.size(); x++) {
            grid.setFalse(points.get(x).getX(), points.get(x).getY());
        }
        points = new ArrayList<Coordinate>();
        N =1;
        n =1;
        if (grid == null) //should this ever happen?
            return;
        grid.setPoints(new LinkedList<Coordinate>());
        hull = new Hull(new Coordinate[]{});

        refresh();
    }

    /**
     * This method is called when there is a touch event.
     *
     * @param event
     * @return boolean Determines if there was a touch event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        this.mDetector.onTouchEvent(event);
        return true;
    }

    /**
     * This method is called whenever something is tapped down.
     *
     * @param event This is the MotionEvent reference.
     * @return boolean This determines if something was pressed.
     */
    @Override
    public boolean onDown(MotionEvent event) {
        Log.d("Sensor", "onDown: " + event.toString());
        return true;
    }

    /**
     * This method is called when a fling occured.
     *
     * @param event1    This is the first event.
     * @param event2    This is the second event.
     * @param velocityX This is the velocity of the starting flick.
     * @param velocityY This is the velocity of the second flick.
     * @return boolean This returns that there was a fling.
     */
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        Log.d("Sensor", "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    /**
     * This method is called when there was a long click.
     *
     * @param event This is the event reference.
     */
    @Override
    public void onLongPress(MotionEvent event) {
    }

    /**
     * This method is used to scroll through the grid.
     *
     * @param e1        This is the first event.
     * @param e2        This is the second event.
     * @param distanceX This is the x distance covered.
     * @param distanceY This is the y distance covered.
     * @return boolean This is the boolean if there has been a scroll.
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        if (!Settings.SHOULD_MOVE)
            return true;
        Log.d("Sensor", "onScroll: " + e1.toString() + e2.toString());
        if (settings.getXStart() + (int) distanceX >= 0)
            setStartX(settings.getXStart() + (int) distanceX);
        if (settings.getYStart() + (int) distanceY >= 0)
            setStartY(settings.getYStart() + (int) distanceY);

        return true;
    }

    /**
     * This method shows whenever something is pressed.
     *
     * @param event
     */
    @Override
    public void onShowPress(MotionEvent event) {
        Log.d("Sensor", "onShowPress: " + event.toString());
    }

    /**
     * This method is called whenever there was a tap up.
     *
     * @param event
     * @return boolean This is true when there is a tap.
     */
    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d("Sensor", "onSingleTapUp: " + event.toString());
        return true;
    }

    public void nextGiftWrap(View v)
    {
        Log.d("GiftWrapNext", "Next");
        //compute?
        Coordinate[] arr = new Coordinate[points.size()];
        for (int x = 0; x < points.size(); x++) {
            arr[x] = points.get(x);
        }

        if(n >= arr.length){
            reset(v);
            return;
        }

        n++;

        // Find the leftmost point
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int value = 0; //minimum value
        for (int x = 0; x < arr.length; x++) {
            if (arr[x].getX() < minX || (arr[x].getX() == minX && arr[x].getY() > minY)) {
                minY = arr[x].getY();
                minX = arr[x].getX();
                value = x;
            }
        }
        //You got the starting point

        Stack<Coordinate> Q = new Stack<>();
        int p = value, q;

        do
        {
            // Add current point to result
            Q.push(arr[p]);

            // Search for a point 'q' such that
            // orientation(p, x, q) is counterclockwise
            // for all points 'x'. The idea is to keep
            // track of last visited most counterclock-
            // wise point in q. If any point 'i' is more
            // counterclock-wise than q, then update q.
            q = (p + 1) % arr.length;

            for (int i = 0; i < arr.length; i++)
            {
                // If i is more counterclockwise than
                // current q, then update q
                Vector p0p1 = new Vector(arr[i].getX() - arr[p].getX(), arr[i].getY() - arr[p].getY());
                Vector p1p2 = new Vector(arr[q].getX() - arr[i].getX(), arr[q].getY() - arr[i].getY());
                if (p0p1.turnsLeft(p1p2)) {
                    q = i;
                }
            }
            // Now q is the most counterclockwise with
            // respect to p. Set p as q for next iteration,
            // so that q is added to result 'hull'
            p = q;

        } while (p != value);  // While we don't come to first
        // point
        int size = Q.size();
        Coordinate[] curr_hull = new Coordinate[n];
        for (int x = 0; x < n; x++) {
            Log.d("Size","stack"+size);
            if(Q.isEmpty()){
                reset(v);
                return;
            } else {
            curr_hull[x] = Q.pop();
            }
        }
        hull = new Hull(curr_hull);
        refresh();

    }


    public void nextGrahamScan(View v){


        Log.d("GrahamScanNext", "Next");
        //compute?
        Coordinate[] arr = new Coordinate[points.size()];
        for (int x = 0; x < points.size(); x++) {
            arr[x] = points.get(x);
        }
        int maxClicks = arr.length;
        if(N >= maxClicks){
            reset(v);
            return;
        }

        N++;

        if (N <= 1) {
            //refresh();
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int value = 0; //minimum value
        for (int x = 0; x < arr.length; x++) {
            if (arr[x].getY() < minY || (arr[x].getY() == minY && arr[x].getX() < minX)) {
                minY = arr[x].getY();
                minX = arr[x].getX();
                value = x;
            }
        }
        //You got the starting point, now you have to sort everything by the y coordinate!

        for (int x = 0; x < arr.length; x++) {
            if (x == value)
                continue;
            if (arr[x].getX() == minX) {
                arr[x].setAngle(0); //fix 0 -> 90 WAIT .. THIS SHOULD BE 90 FIX?
            } else {
                arr[x].setAngle(Math.atan2((double) (arr[x].getY() - minY), (double) (arr[x].getX() - minX)));
            }
        }

        Arrays.sort(arr);
        Log.d("Sort", "after sorting");
        Stack<Coordinate> Q = new Stack<>();
        Stack<Coordinate> tempQ = new Stack<>();
        Q.push(arr[0]);
        Q.push(arr[1]);
        for (int x = 2; x < N; x++) {

            Q.push(arr[x]);
            if (N == 3) //odd..
                break;
            while (true) {
                //check to see if arr[0]arr[1] vector angle with arr[1][2] is a right
                if (Q.size() < 3) {
                    break;

                }
                Coordinate p2 = Q.pop();
                Coordinate p1 = Q.pop();
                Coordinate p0 = Q.pop();
                Vector p0p1 = new Vector(p1.getX() - p0.getX(), p1.getY() - p0.getY());
                Vector p1p2 = new Vector(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                if (p0p1.turnsLeft(p1p2)) {
                    Q.push(p0);
                    Q.push(p1);
                    Q.push(p2);
                    break;
                } else {
                    //remove the last candidate
                    Q.push(p0);
                    Q.push(p2);
                }
                int size = Q.size();
                Coordinate[] curr_hull = new Coordinate[size];
                tempQ.addAll(Q);
                for (int y = 0; y < size; y++)
                    curr_hull[y] = tempQ.pop();

                hull = new Hull(curr_hull);
                refresh();
                // Actions to do after 1/2 seconds
            }
        }

        int size = Q.size();
        Coordinate[] curr_hull = new Coordinate[size];
        for (int x = 0; x < size; x++)
            curr_hull[x] = Q.pop();
        Log.d("Sensor", "Final update to hull ");
        hull = new Hull(curr_hull);
        refresh();
    }

    /**
     * This method is called whenever there is a double tap.
     *
     * @param event This is the event reference variable.
     * @return This method returns if there was a double tap.
     */



    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d("Sensor", "onDoubleTap: " + event.toString());
        //compute?
//        Coordinate[] arr = new Coordinate[points.size()];
//        for (int x = 0; x < points.size(); x++) {
//            arr[x] = points.get(x);
//        }
//        GrahamScan solve = new GrahamScan(arr); //return the size?
//        hull = new Hull(solve.solve());   //solve() returns a coordinate array
//        refresh();
        return true;
    }

    /**
     * This method returns if there was a double tap.
     *
     * @param event
     * @return boolean Determines if there was a double tap.
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d("Sensor", "onDoubleTapEvent: " + event.toString());
        return true;
    }

    /**
     * This method adds a point to the graph upon a single tap.
     * If skip is enabled then it calculates the appropriate point. Else, it will simply populate the grid.
     *
     * @param event This is the event reference.
     * @return boolean This is the return value.
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d("SensorTap", "Tap!" + event.toString());
        //add here...
        int xPos = (int) event.getRawX(); //why? wtf lol
        int yPos = (int) event.getRawY() - 150;
        yPos = Math.max(0, yPos);

        /* Error trap :) */
        if (xPos > GridView.width) {
            xPos = GridView.width - 1;
        }

        if (yPos > GridView.height) {
            yPos = GridView.height - 1;
        }

        int xIndex = (xPos / GridView.getSkip() + gridRenderer.getXStart());
        int yIndex = (yPos / GridView.getSkip() + gridRenderer.getYStart());

        if (grid == null) {
            grid = new Grid(gridRenderer.getWidth(), gridRenderer.getHeight());
        }
        Log.d("Grid", xIndex + " " + yIndex + " : " + xPos + " " + yPos);
        if (Settings.SHOULD_MOVE)
            grid.setTrue(xIndex, yIndex); //update the grid this way... NOW CHANGE THE RENDERING FOR THE GRID SYSTEM!
        else
            grid.setTrue(xPos, yPos); //good enough

        points.add(new Coordinate(xPos, yPos, GridView.getSkip())); //change coordinates? ADD GET SKIP -> OK
        grid.getPoints().add(new Coordinate(xPos, yPos, 1)); //standard...
        gridRenderer = new GridView(this, grid, settings);
        setContentView(R.layout.activity_main);
        LinearLayout upper = (LinearLayout) findViewById(R.id.LinearLayout01);
        upper.addView(gridRenderer);
        return true;
    }

    /**
     * This method refreshes the screen.
     * IT only resets the top part with the hull.
     */
    public void refresh() {
        if (grid == null) {
            grid = new Grid(gridRenderer.getWidth(), gridRenderer.getHeight());
        }
        gridRenderer = new GridView(this, grid, settings);
        gridRenderer = new GridView(this, grid, settings);
        setContentView(R.layout.activity_main);

        LinearLayout upper = (LinearLayout) findViewById(R.id.LinearLayout01);

        upper.addView(gridRenderer);
    }

    /**
     * This method is called whenever the app is restarted, ie, from sleep or multi-tasking.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        refresh();
    }
}
