package com.rabor.tipcalculator;

import java.text.NumberFormat;

import com.rabor.tipcalculator.R;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnKeyListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.lang.StrictMath;
import android.widget.AdapterView.OnItemSelectedListener;

public class TipCalculatorActivity extends Activity 
/*implements OnEditorActionListener, OnClickListener, RadioGroup.OnCheckedChangeListener */{

    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;   
    private Button   percentUpButton;
    private Button   percentDownButton;
    private Button   applyButton;
    private TextView tipTextView;
    private TextView totalTextView;
    private Spinner splitBillSpinner;
    private RadioGroup roundingRadioGroup;
    private RadioButton noRoundingRadioButton;
    private RadioButton tipRoundingRadioButton;
    private RadioButton totalRoundingRadioButton;
    private SeekBar percentSeekbar;
    private TextView perPersonAmountTextView;
    private TextView perPersonLabel;
    private final int NO_ROUNDING = 1;
    private final int TIP_ROUNDING = 2;
    private final int TOTAL_ROUNDING = 3;
    private int rounding = 1;
    private int split = 1;
    // define the SharedPreferences object
    private SharedPreferences savedValues;
    

    private String billAmountString = "";
    private float tipPercent = .15f;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        // get references to the widgets
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
     //   percentUpButton = (Button) findViewById(R.id.percentUpButton);
     //   percentDownButton = (Button) findViewById(R.id.percentDownButton);
     //   applyButton = (Button) findViewById(R.id.applyButton);
        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        perPersonAmountTextView = (TextView) findViewById(R.id.perPersonAmountTextView);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        //get reference to Seekbar
        percentSeekbar = (SeekBar) findViewById(R.id.percentSeekbar);
        //get references for radio group and buttons
        roundingRadioGroup = (RadioGroup) findViewById(R.id.roundingRadioGroup);
        noRoundingRadioButton = (RadioButton) findViewById(R.id.noRoundingRadioButton);
        tipRoundingRadioButton = (RadioButton) findViewById(R.id.tipRoundingRadioButton);
        totalRoundingRadioButton = (RadioButton) findViewById(R.id.totalRoundingRadioButton);

        //get reference for spinner
        splitBillSpinner = (Spinner) findViewById(R.id.splitBillSpinner);

        // set up spinner and array adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.split_array, android.R.layout.simple_spinner_item);
        splitBillSpinner.setAdapter(adapter);

        //set spinner to first item in selection list
        splitBillSpinner.setSelection(0);

        //set default rounding selection
        noRoundingRadioButton.setChecked(true);


        // wire the listeners
        billAmountEditText.setOnEditorActionListener(editorAction);
        billAmountEditText.setOnKeyListener(keyListener);
     //   percentUpButton.setOnClickListener(clickListener);
     //   percentDownButton.setOnClickListener(clickListener);
     //   applyButton.setOnClickListener(clickListener);
        roundingRadioGroup.setOnCheckedChangeListener(checkChangeListener);
        splitBillSpinner.setOnItemSelectedListener(itemSelected);
        percentSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        percentSeekbar.setOnKeyListener(keyListener);
        // get SharedPreferences object
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);

        // set up anonymous event classes

    }
    
    @Override
    public void onPause() {
        // save the instance variables       
        Editor editor = savedValues.edit();        
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.putInt("rounding", rounding);
        editor.putInt("split", split);
        editor.commit();        

        super.onPause();      
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", 0.15f);
        rounding = savedValues.getInt("rounding", 1);
        split = savedValues.getInt("split", 1);
        // set the bill amount on its widget
        billAmountEditText.setText(billAmountString);
        
        // calculate and display
        calculateAndDisplay();
    }    
    
    public void calculateAndDisplay() {        

        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount = Float.parseFloat(billAmountString);
        float totalAmount = 0.0f;
        float tipAmount = 0.0f;
        float amountPerPerson = 0.0f;

        // calculate tip and total - and do the rounding, if any
        if (/*noRoundingRadioButton.isChecked()*/ rounding == NO_ROUNDING)
        {
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
        }
        else if (/*tipRoundingRadioButton.isChecked()*/ rounding == TIP_ROUNDING)
        {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
        }
        else if (/*totalRoundingRadioButton.isChecked()*/ rounding == TOTAL_ROUNDING)
        {
            tipAmount = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipAmount);
        }

        //divide bill per person - if splitting is selected from the spinner
        switch (/*splitBillSpinner.getSelectedItemPosition()*/ split)
        {
            case 1:
                perPersonLabel.setVisibility(View.GONE);
                perPersonAmountTextView.setVisibility(View.GONE);
                break;
            case 2:
                amountPerPerson = totalAmount / 2;
                perPersonLabel.setVisibility(View.VISIBLE);
                perPersonAmountTextView.setVisibility(View.VISIBLE);
                break;
            case 3:
                amountPerPerson = totalAmount / 3;
                perPersonLabel.setVisibility(View.VISIBLE);
                perPersonAmountTextView.setVisibility(View.VISIBLE);
                break;
            case 4:
                amountPerPerson = totalAmount / 4;
                perPersonLabel.setVisibility(View.VISIBLE);
                perPersonAmountTextView.setVisibility(View.VISIBLE);
                break;
            default:
                break;

        }


        // display the other results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        perPersonAmountTextView.setText(currency.format(amountPerPerson));

        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
    }
    
  /*  @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
    		actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }        
        return false;
    }*/

    // event listener anonymous class methods
  /*  public OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.percentDownButton:
                    tipPercent = tipPercent - .01f;
                    calculateAndDisplay();
                    break;
                case R.id.percentUpButton:
                    tipPercent = tipPercent + .01f;
                    calculateAndDisplay();
                    break;
                case R.id.applyButton:
                    calculateAndDisplay();
                    break;
            }

        }
    }; */

    private OnKeyListener keyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode){

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                //inputMethodManager hides the soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(billAmountEditText.getWindowToken(), 0);
                //consume event
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (v.getId() == R.id.percentSeekbar){
                    calculateAndDisplay();
                }
                break;

            }
            //otherwise, don't consume the event
            return false;
        }
    };

    private OnEditorActionListener editorAction = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                calculateAndDisplay();
            }
            return false;

        }
    };

 /*   @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }*/
    private OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener(){
     @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId)
            {
                case R.id.noRoundingRadioButton:
                    rounding = NO_ROUNDING;
                    break;
                case R.id.tipRoundingRadioButton:
                    rounding = TIP_ROUNDING;
                    break;
                case R.id.totalRoundingRadioButton:
                    rounding = TOTAL_ROUNDING;
            }
            calculateAndDisplay();

        }
    };
    private OnItemSelectedListener itemSelected = new OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            split = position + 1;
            calculateAndDisplay();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            percentTextView.setText(percentSeekbar.getProgress() + "%");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            float progress = percentSeekbar.getProgress();
            tipPercent = progress / 100;
            calculateAndDisplay();
        }
    };

}