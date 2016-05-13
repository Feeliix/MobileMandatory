package org.projects.shoppinglist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.io.Console;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity  {


    //ArrayAdapter<Product> adapter;
    ListView listView;
    //ArrayList<Product> bag = new ArrayList<Product>();
    int position;
    FirebaseListAdapter<Product> fireAdapter;
    Firebase firebase;
    public FirebaseListAdapter<Product> getMyAdapter()
    {
        return fireAdapter;
    }
    //Product lastDeletedProduct;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.list);
        // Creating the firebase adapter
        firebase = new Firebase("https://shoppinglistfelix.firebaseio.com/items/");
        Firebase ref = new Firebase("https://shoppinglistfelix.firebaseio.com/items/");
        final FirebaseListAdapter<Product> fireAdapter = new FirebaseListAdapter<Product>(
                this,
                Product.class,
                android.R.layout.simple_list_item_checked,
                ref
        ) {
            @Override
            protected void populateView(View view, Product product, int i) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                textView.setText(product.toString());

            }
        };


        //getting our listiew - you can check the ID in the xml to see that it
        //is indeed specified as "list"

        //here we create a new adapter linking the bag and the
        //listview


        //setting the adapter on the listview
        listView.setAdapter(fireAdapter);
        //here we set the choice mode - meaning in this case we can
        //only select one item at a time.
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        Button addButton = (Button) findViewById(R.id.addButton);
        assert addButton != null;
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editItem = (EditText) findViewById(R.id.item);
                final EditText editNumber = (EditText) findViewById(R.id.itemNumber);
                int i = Integer.parseInt(editNumber.getText().toString());
                String s = editItem.getText().toString();
                Product p = new Product(s, i);
                firebase.push().setValue(p);

                //The next line is needed in order to say to the ListView
                //that the data has changed - we have added stuff now!
                fireAdapter.notifyDataSetChanged();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Item added!", Toast.LENGTH_LONG);
                toast.show();


            }
        });



        // delete single item button
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        assert deleteButton != null;
        deleteButton.setOnClickListener(new View.OnClickListener() {
        /** Called when the user clicks the Send button */
        @Override
        public void onClick(View v) {
            int pos = listView.getCheckedItemPosition();
            if (pos<0){
                return;
            }
            saveCopy();

            fireAdapter.getRef(pos).setValue(null);
            fireAdapter.notifyDataSetChanged();
            listView.setItemChecked(-1, true);
            final View parent = listView;
            Snackbar snackbar = Snackbar
                    .make(parent, "Item Deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            firebase.push().setValue(lastDeletedProduct);
                            fireAdapter.notifyDataSetChanged();
                            Snackbar snackbar = Snackbar.make(parent, "Item restored!", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    });

            snackbar.show();
        }
        });

        if (savedInstanceState!=null)
        {
//            position = savedInstanceState.getInt("position");
//            ArrayList<Product> theList = savedInstanceState.getParcelableArrayList("savedList");
//            if (listView!=null) //not null, so something was saved
//            {
//                bag.clear();
//                bag.addAll(theList);// = listView;
//            }
            if (position!=ListView.INVALID_POSITION)
            {
                listView.setItemChecked(position,true);
            }
            //we need to set the text field
            //try to comment the above line out and
            //see the effect after orientation change (after saving some name)
        }
        //initialize our text field
       // listView = (ListView) findViewById(R.id.list);



    }
    Object lastDeletedProduct;
    int lastDeleted;
    public void saveCopy()
    {
        lastDeleted = listView.getCheckedItemPosition();
        lastDeletedProduct = listView.getAdapter().getItem(lastDeleted);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getPreferences();
            return true;
        }
        if (id == R.id.delete_all_items) {
            showDialog();
            return true;
        }
        if (id == R.id.share_list) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain"); //MIME type
            String textToShare = "I need these items\n" + convertListToString();
            intent.putExtra(Intent.EXTRA_TEXT, textToShare); //add the text to t
            startActivity(intent);
            return true;
        }
        if (id == R.id.order) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain"); //MIME type
            String textToShare = "Contact Information\n" + prefsToString() + "\nItems on the list:\n" + convertListToString();
            intent.putExtra(Intent.EXTRA_TEXT, textToShare); //add the text to t
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //This method is called before our activity is destroyed
    protected void onSaveInstanceState(Bundle outState) {
        //ALWAYS CALL THE SUPER METHOD
        super.onSaveInstanceState(outState);
		/* Here we put code now to save the state */
        //outState.putParcelableArrayList("savedList", bag);
        int pos = listView.getCheckedItemPosition();
        outState.putInt("position", pos);
        position = pos;

    }

    //This is the event handler for the show button
    //This is specified in the xml file that this should
    //be the event handler
    public void showDialog() {
        //showing our dialog.
        MyDialogFragment dialog = new MyDialogFragment() {
            @Override
            protected void positiveClick() {
                //Here we override the methods and can now
                //do something
                Firebase fireItems = new Firebase("https://shoppinglistfelix.firebaseio.com/");
                fireItems.setValue(null);
                //getMyAdapter().notifyDataSetChanged();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "positive button clicked", Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void negativeClick() {
                //Here we override the method and can now do something
                Toast toast = Toast.makeText(getApplicationContext(),
                        "negative button clicked", Toast.LENGTH_SHORT);
                toast.show();
            }
        };

        //Here we show the dialog
        //The tag "MyFragement" is not important for us.
        dialog.show(getFragmentManager(), "MyFragment");
    }
    public void getPreferences() {

        Intent intent = new Intent(this, SettingsActivity.class);
        //startActivity(intent); //this we can use if we DONT CARE ABOUT RESULT

        //we can use this, if we need to know when the user exists our preference screens
        startActivityForResult(intent, 1);
    }
    public String prefsToString(){

        String contact = "";
        SharedPreferences prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String address = prefs.getString("address", "");
        String postcode = prefs.getString("postcode", "");
        boolean orderEnabled = prefs.getBoolean("order", false);
        boolean cityEnabled = prefs.getBoolean("country", false);
        contact = "Name: " + name + "\nGender: " + address + "\nPostcode: " + postcode + "\n" + "\nOrder confirmation: " + orderEnabled + "\nLives in Århus: " + cityEnabled;
        return  contact;
    }
    public String convertListToString()
    {
        String result = "";
        for (int i = 0; i<listView.getAdapter().getCount();i++)
        {
            Product p = (Product) listView.getAdapter().getItem(i);
            result = result + p.toString() + "\n";
        }
        return result;
    }


}
