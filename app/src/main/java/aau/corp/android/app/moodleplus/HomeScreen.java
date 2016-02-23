package aau.corp.android.app.moodleplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////
// This is the home screen when you are logged in correctly
// This is screen displays your notifications, assignments, grades in various subjects as well as your courses
// To show your courses, a dropdown list has been enabled which on click opens and show your registered courses
// Notifications, grades and assignments takes you to the new activity of the respective options
///////////////////////////////////

public class HomeScreen extends AppCompatActivity {

    /////////////////////////////////////
    // Setting up new variables
    ///////////////////////////////////
    public TextView to_grades;
    public List<String> course_list_with_name_1 = new ArrayList<>();
    public List<String> course_list_codes_1 = new ArrayList<>();
    public List<String> course_list_codes_2 = new ArrayList<>();

    ///////////////////////////////////
    // This creates a list for the courses and list opens up if you tap on it
    ///////////////////////////////////
    HashMap<String, List<String>> my_courses = new HashMap<String, List<String>>();
    List<String> courses_list;
    ExpandableListView my_course_list;
    courseAdapter adapter_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        ///////////////////////////////////
        // Calling the send request function to send the request to the server
        ///////////////////////////////////
        sendRequest();

        ///////////////////////////////////
        // Function for the 3 dot button in the top right position
        ///////////////////////////////////
        getOverflowMenu();
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        ///////////////////////////////////
        // Button Click Listener for grades button has been called
        ///////////////////////////////////
        onButtonClickListener_grades();

    }


    ///////////////////////////////////
    // This function is for back pressed button
    // If you are logged in and you press back, an alert dialogue box will appear and show Yes and No options
    // On Clicking Yes, you will exit the application
    // On Clicking No, nothing will happen and you will be back at Home Screen
    ///////////////////////////////////
    @Override
    public void onBackPressed(){

        // calls the alert dialogue box
        AlertDialog.Builder submit_alert = new AlertDialog.Builder(HomeScreen.this);
        submit_alert.setMessage("Are you sure you want to exit !!!").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // calls the function which send the request
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {    // If no is pressed, you are taken back to the Home screen
                dialog.cancel();
            }
        });

        AlertDialog alert = submit_alert.create();
        alert.setTitle("Alert !!!");
        alert.show();




/*
        if(PressTwice){

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);

        }

        PressTwice = true;
        Toast.makeText(this, "Press BACK Button again to Exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PressTwice = false;
            }
        }, 2000);
*/
    }


    private void onButtonClickListener_grades() {
        to_grades = (TextView) findViewById(R.id.to_grades);
        to_grades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent grade_page = new Intent(HomeScreen.this, GradeScreen.class);
                startActivity(grade_page);
            }
        });
    }

    ///////////////////////////////////
    // This function is for the three dots button where a small menu open up which displays several items like your name, entry number, profile, signout optons
    ///////////////////////////////////
    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////
    // This functions inflate the 3 dot menu button with items present in the menu file
    ///////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }


    ///////////////////////////////////
    // This function handles the selection of options from the 3-dot menu option
    ///////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        ///////////////////////////////////
        // Function if You click on The Profie Item
        ///////////////////////////////////
        if (id == R.id.profile) {
            Intent profile_page = new Intent(getApplicationContext(), ProfileScreen.class);
            Integer user_id=  getIntent().getExtras().getInt("user");
            profile_page.putExtra("user",user_id);
            startActivity(profile_page);
        }
        return false;
    }


    ///////////////////////////////////
    // Function to send request to the server
    ///////////////////////////////////
    private void sendRequest() {

        //Url for Courses list
        String adder1 = IPAddress.getName();
        String url = "http://" + adder1 + "/courses/list.json";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        PJson(response);
                       //Toast.makeText(HomeScreen.this, "we get a response for the courses", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeScreen.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        // Get a RequestQueue
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(request);
    }

    ///////////////////////////////////
    // Getting the response from server, extracting the response and showing it up on the xml file
    ///////////////////////////////////
    public void PJson(final String response){

        try {
            JSONObject course_details = new JSONObject(response);
            JSONArray json_array_course_request =   course_details.getJSONArray("courses");

            course_list_codes_1.add("Home");
            if( json_array_course_request.length()!=0){

                for (int i = 0; i < json_array_course_request.length(); i++) {
                    JSONObject childJSONObject = json_array_course_request.getJSONObject(i);

                    course_list_codes_1.add(childJSONObject.getString("code").toUpperCase());
                    course_list_with_name_1.add(childJSONObject.getString("code").toUpperCase() + " : " + childJSONObject.getString("name"));

                }

                my_courses.put("MY COURSES", course_list_with_name_1);
                course_list_codes_2 = course_list_codes_1;

                my_course_list = (ExpandableListView) findViewById(R.id.my_course_expan_list);

                courses_list = new ArrayList<String>(my_courses.keySet());
                adapter_list = new courseAdapter(this, my_courses, courses_list);
                my_course_list.setAdapter(adapter_list);

                final String response_to_be_sent_to_next_activity = response;

                my_course_list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                        int ls = childPosition;

                        Intent transition = new Intent(getApplicationContext(), Courses.class);
                        transition.putExtra("Item_Number", ls);
                        transition.putExtra("response", response);
                        transition.putStringArrayListExtra("Course_Code_List", (ArrayList<String>) course_list_codes_2);
                        transition.putExtra("response", response);
                        startActivity(transition);
                        return false;
                    }
                });


            }
            else{
                Toast.makeText(HomeScreen.this, "the array is null", Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}

