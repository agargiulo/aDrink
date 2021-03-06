package edu.rit.csh.aDrink;

/*
 * CSH Drink App by Andrew Hanes
 */

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;

public class Drink_Main extends Activity
{
	public Connector drinkServ;
	SharedPreferences sp;
	SharedPreferences.Editor edit;
	Head title;
	LinearLayout linearLayout;

	/*
	 * Switches to another server. Current options are ld, d, s
	 * 
	 * @precondition Drink Server has been initialized
	 */
	public void changeMachine (String machine)
	{
		this.drinkServ.command("machine " + machine);
	}

	/*
	 * Promts user for new server
	 * 
	 */
	public void changeMachineAlert ()
	{
		AlertDialog.Builder changeServerDiag = new AlertDialog.Builder(this);
		changeServerDiag.setTitle("Change Machine");
		changeServerDiag.setMessage("Select New Vending Machine");
		final RadioButton ld = new RadioButton(this);
		ld.setText("Little Drink");
		ld.setId(1);
		final RadioButton s = new RadioButton(this);
		s.setText("Snack");
		s.setId(2);
		final RadioButton d = new RadioButton(this);
		d.setText("Drink");
		d.setId(3);
		final RadioGroup rg = new RadioGroup(this);
		rg.addView(ld);
		rg.addView(s);
		rg.addView(d);
		changeServerDiag.setView(rg);
		changeServerDiag.setPositiveButton("Ok",
				new DialogInterface.OnClickListener()
				{
					public void onClick (DialogInterface dialog, int whichButton)
					{
						String newServ;
						int n = rg.getCheckedRadioButtonId();
						if(n == 1)
						{
							newServ = "ld";
						} else if(n == 2)
						{
							newServ = "s";
						} else
						{
							newServ = "d";
						}
						edit.putString("serv", newServ);
						edit.commit();
						updateButtons();
					}
				});
		changeServerDiag.setNegativeButton("Cancel", new OnClickListener()
		{
			public void onClick (DialogInterface dialog, int whichButton)
			{
				// Don't do anything
			}

		});
		changeServerDiag.show();
		title.update();
	}

	/*
	 * Prompts a user to enter a password and logs them in
	 * 
	 * @precondition Drink Server has been initialized
	 */
	public void changePasswordAlert ()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Password");
		alert.setMessage("Enter Password for Account "
				+ sp.getString("user", "null"));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
				| InputType.TYPE_CLASS_TEXT);
		TableLayout tb = new TableLayout(this);
		tb.addView(input);
		final CheckBox cb = new CheckBox(this);
		cb.setText("Remember Password?");
		tb.addView(cb);
		alert.setView(tb);
		alert.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey (DialogInterface dialog, int keyCode,
					KeyEvent event)
			{
				if(keyCode == KeyEvent.KEYCODE_ENTER)
				{
					drinkServ.command("USER " + sp.getString("user", "null"));
					ArrayList<String> temp = drinkServ.command("PASS "
							+ input.getText().toString());
					if(temp.get(0).toLowerCase().indexOf("err") == -1)
					{
						// Log.d("LOGIN", "LOGIN Succeeded");
						title.update();
						if(cb.isChecked())
						{
							edit.putString("pass", input.getText().toString());
							edit.commit();
						}
						dialog.dismiss();
						return true;
					} else
					{
						dialog.dismiss();
						changePasswordAlert();
						displayAlert("Invalid Username/Password");
					}
				}
				return false;

			}

		});
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int whichButton)
			{
				drinkServ.command("USER " + sp.getString("user", "null"));
				ArrayList<String> temp = drinkServ.command("PASS "
						+ input.getText().toString());
				if(temp.get(0).toLowerCase().indexOf("err") == -1)
				{
					title.update();
					if(cb.isChecked())
					{
						edit.putString("pass", input.getText().toString());
						edit.commit();
					}
					return;
				} else
				{
					changePasswordAlert();
					displayAlert("Invalid Username/Password");
				}
			}
		});
		alert.show();
	}

	/*
	 * Opens a dialog box and prompts user for new username and new password
	 * 
	 * @precondition Drink Server has been initialized
	 */
	public void changeUsernameAlert ()
	{
		final AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
		alert2.setTitle("Username");
		alert2.setMessage("Enter New Username");
		final EditText input2 = new EditText(this);
		alert2.setView(input2);
		alert2.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey (DialogInterface dialog, int keyCode,
					KeyEvent event)
			{
				if(keyCode == KeyEvent.KEYCODE_ENTER)
				{
					edit.putString("user", input2.getText().toString());
					// Log.d("USER", input2.getText().toString());
					edit.commit();
					changePasswordAlert();
					dialog.dismiss();
					return true;
				}
				return false;

			}

		});
		alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int whichButton)
			{
				edit.putString("user", input2.getText().toString());
				// Log.d("USER", input2.getText().toString());
				edit.commit();
				changePasswordAlert();
			}
		});
		alert2.show();
	}

	/*
	 * Displays an alert
	 * 
	 * @param  text  The message to be displayed
	 * 
	 */
	public void displayAlert (String text)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Alert");
		alert.setMessage(text);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int whichButton)
			{

			}
		});
		alert.show();
	}

	/*
	 * Returns an ArrayList that contains buttons for all of the drinks
	 * 
	 * @precondition Drink Server has been initialized
	 */
	public ArrayList<DrinkButton> getButtons (Connector drinkServ)
	{
		ArrayList<String> drinks = drinkServ.command("stat");
		ArrayList<DrinkButton> buttons = new ArrayList<DrinkButton>();
		int price, slot, count, place;
		price = -256;
		slot = -256;
		count = -256;
		place = 1;
		String name = "";
		try
		{

			for(String s : drinks)
			{
				String[] temp = s.split("\\s+");
				place = 1;
				name = "";
				while(temp[place].charAt(temp[place].length() - 1) != '"')
				{
					name += temp[place] + " ";
					place ++;
				}
				name += temp[place];
				place ++;
				slot = Integer.parseInt(temp[0].trim());
				price = Integer.parseInt(temp[place].trim());
				count = Integer.parseInt(temp[place + 1].trim());
				buttons.add(new DrinkButton(this, name, price, count, slot,
						drinkServ, this));
			}
			title.update();
		} catch(Exception e)
		{
			Log.e(e.toString(), e.toString());
			buttons.add(new DrinkButton(this, slot + " ERROR: " + name, price,
					count, slot, drinkServ, this));
		}
		if(buttons.size() > 0)
		{
			buttons.remove(buttons.size() - 1);
		}
		return buttons;
	}

	/*
	 * Logs the currently logged in user out by removing 
	 * all user info and then reconnecting to the server
	 * 
	 * @precondition Drink Server has been initialized
	 */
	public void logout ()
	{
		edit.remove("user");
		edit.remove("pass");
		edit.commit();
		drinkServ.reConnect();
		title.update();
	}

	/*
	 * Called when the screen is rotated.  Doesn't need to be called within this 
	 * code, the phone will handle it
	 * 
	 * Without this method onCreate() is called again, which is messy as hell
	 */
	@Override
	public void onConfigurationChanged (Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		this.linearLayout.removeAllViews();
		setContentView(R.layout.main);
		this.linearLayout = (LinearLayout) findViewById(R.id.widget43);
		linearLayout.addView(title);
		linearLayout.setBackgroundColor(Color.GRAY);
		linearLayout.setPadding(20, 10, 20, 10);
		updateButtons();
		// Log.d("Done", "Done w/ onConfigurationChanged");
	}

	/** Called when the activity is first created. */
	@Override
	/*
	 * Sets up the GUI and connector class.
	 * This method is called when the app is first started
	 */
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.linearLayout = (LinearLayout) findViewById(R.id.widget43);
		sp = this.getSharedPreferences("drinkPrefs", MODE_PRIVATE);
		edit = sp.edit();
		this.drinkServ = new Connector(sp.getString("serv", "drink")
				+ ".csh.rit.edu", 4242);
		if(drinkServ.skt == null)
		{
			this.serverDown();
		} else
		{
			this.title = new Head(this, this, drinkServ);
			if(!sp.contains("user"))
			{
				this.changeUsernameAlert();
			} else if(!sp.contains("pass"))
			{
				this.changePasswordAlert();
			} else
			{
				drinkServ.command("USER " + sp.getString("user", "null"));
				drinkServ.command("PASS " + sp.getString("pass", "null"));
				// Log.d("Existing User/Pass","Found existing username/passwod "+sp.getString("user",
				// "....nevermind") + "    " + sp.getString("pass",
				// "....nevermind"));
			}
			this.displayAlert("WARNING: This app does NOT USE SSL!!!\nUse it at your own risk!\n\n"
					+ "Also, you're responsible for all data charges that result from using this application");
			linearLayout.addView(title);
			linearLayout.setBackgroundColor(Color.GRAY);
			linearLayout.setPadding(20, 10, 20, 10);
			updateButtons();
			// Log.d("Done", "Done w/ onCreate");
		}
	}

	/*
	 * Uses options_menu.xml to create an options menu
	 * 
	 * This method is run by the phone
	 */
	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.layout.options_menu, menu);
		return true;
	}

	@Override
	public void onDestroy ()
	/*
	 * Runs when the app is closed
	 * The phone will take care of running it
	 */
	{
		super.onDestroy();
		// Log.d("Goodbye Cruel World!!!!", "Exiting");
		drinkServ.close();
	}

	/*
	 * What should be done based on the id of the menu item selected
	 */
	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.delay:
			this.setDelay();
			return true;
		case R.id.chserv:
			this.changeMachineAlert();
			return true;
		case R.id.chusr:
			this.logout();
			this.changeUsernameAlert();
			return true;
		case R.id.logout:
			this.logout();
			this.displayAlert("User Credentials Wiped");
			return true;
		}
		return false;
	}

	/*
	 * Displays a message if the server is down
	 */
	public void serverDown ()
	{
		AlertDialog.Builder serverDownDiag = new AlertDialog.Builder(this);
		serverDownDiag.setTitle("Server is down");
		serverDownDiag
				.setMessage("Server is down\nGo bug McGary\n(Or I can't connect to drink)");
		serverDownDiag.setPositiveButton("Ok",
				new DialogInterface.OnClickListener()
				{
					public void onClick (DialogInterface dialog, int whichButton)
					{

					}
				});
		serverDownDiag.show();
	}

	/*
	 * Opens an AlertDialog promoting the user to enter a delay
	 * 
	 */
	public void setDelay ()
	{
		AlertDialog.Builder inputDiag = new AlertDialog.Builder(this);
		inputDiag.setTitle("Enter Delay");
		inputDiag.setMessage("Enter Delay (int)\nCurrent delay = "
				+ sp.getInt("delay", 0));
		final EditText input = new EditText(this);
		inputDiag.setView(input);
		inputDiag.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey (DialogInterface dialog, int keyCode,
					KeyEvent event)
			{
				if(keyCode == KeyEvent.KEYCODE_ENTER)
				{
					try
					{
						int n = Integer.parseInt(input.getText().toString());
						if(n < 1000 && n > -1)
						{
							edit.putInt("delay", n);
						} else
						{
							displayAlert("Invalid value\nRequires -1 < Delay < 1000");
							edit.putInt("delay", 0);
						}
					} catch(Exception e)
					{
						edit.putInt("delay", 0);
						displayAlert("Invalid value\n Requires 0 < n < 1000");
					}
					edit.commit();
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
		inputDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int whichButton)
			{
				int n = 0;
				try
				{
					n = Integer.parseInt(input.getText().toString());

				} catch(Exception e)
				{
					n = 0;
					displayAlert("Invalid value\n Requires 0 < n < 1000");
				}
				if(n < 1000 && n >= 0)
				{
					edit.putInt("delay", n);
				} else
				{
					displayAlert("Invalid value\nRequires 0 < Delay < 1000");
					edit.putInt("delay", 0);
				}
				edit.commit();

			}
		});
		inputDiag.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener()
				{
					public void onClick (DialogInterface dialog, int whichButton)
					{

					}

				});
		inputDiag.show();
	}

	/*
	 * Creates buttons
	 * 
	 * @precondition linearLayout has been initialized, drinkServ has been 
	 * initialized, title has been initialized
	 */
	public void updateButtons ()
	{
		this.changeMachine(sp.getString("serv", "d"));
		linearLayout.removeAllViews();
		linearLayout.addView(title);
		ArrayList<DrinkButton> buttons = this.getButtons(this.drinkServ);
		for(DrinkButton dB : buttons)
		{
			linearLayout.addView(dB);
		}
		title.update();
	}
}