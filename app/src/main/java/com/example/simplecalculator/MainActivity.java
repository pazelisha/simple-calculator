package com.example.simplecalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewHistory;
    private TextView textViewDisplay;

    private StringBuilder currentInput;
    private String operator = "";
    private double operand1 = Double.NaN;
    private double operand2 = Double.NaN;
    private boolean operatorPressed = false;
    private boolean calculationDone = false;

    private DecimalFormat decimalFormat;

    private final int colorBlack = Color.BLACK;
    private int colorGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewHistory = findViewById(R.id.textViewInput);
        textViewDisplay = findViewById(R.id.textViewResult);

        currentInput = new StringBuilder();
        decimalFormat = new DecimalFormat("#.##########");
        colorGray = ContextCompat.getColor(this, R.color.gray_text);

        clearCalculator();
        setupButtonClickListeners();

        Button buttonBackspace = findViewById(R.id.buttonBackspace);
        buttonBackspace.setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
            else if (!operator.isEmpty()) {
                currentInput.append(decimalFormat.format(operand1));
                operand1 = Double.NaN;
                operator = "";
                operatorPressed = false;
            }
            updateMainDisplayExpression();
        });
    }

    private void setupButtonClickListeners() {
        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
                R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonEquals, R.id.buttonClear
        };
        for (int id : buttonIds) {
            View button = findViewById(id);
            if (button != null && button.isClickable()) {
                button.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.buttonClear) {
            clearCalculator();
        } else if (id == R.id.buttonEquals) {
            handleEqualsClick();
        } else if (id == R.id.buttonAdd || id == R.id.buttonSubtract ||
                id == R.id.buttonMultiply || id == R.id.buttonDivide) {
            handleOperatorClick(((Button) v).getText().toString());
        } else {
            handleDigitClick(((Button) v).getText().toString());
        }
    }

    private void handleDigitClick(String digit) {
        if (calculationDone) {
            clearCalculator();
        }

        if (operatorPressed) {
            operatorPressed = false;
        }

        if (currentInput.length() == 1 && currentInput.toString().equals("0")) {
            if (digit.equals("0")) return;
            else currentInput.setLength(0);
        }
        if (textViewDisplay.getText().toString().equals("0") && digit.equals("0") && currentInput.length()==0 && Double.isNaN(operand1)) {
            return;
        }

        currentInput.append(digit);
        updateMainDisplayExpression();
    }

    private void handleOperatorClick(String newOperator) {
        if (calculationDone) {
            operator = newOperator;
            currentInput.setLength(0);
            operatorPressed = true;
            calculationDone = false;
            textViewHistory.setText("");
            updateMainDisplayExpression();
            return;
        }

        if (operatorPressed) {
            operator = newOperator;
            updateMainDisplayExpression();
            return;
        }

        if (currentInput.length() > 0 && Double.isNaN(operand1)) {
            try {
                operand1 = Double.parseDouble(currentInput.toString());
                currentInput.setLength(0);
                operator = newOperator;
                operatorPressed = true;
                updateMainDisplayExpression();
            } catch (NumberFormatException e) {
                showError("Invalid Number");
            }
        }
        else if (!Double.isNaN(operand1)) {
            if (currentInput.length() > 0) {
                double result = performCalculation();
                if (!Double.isNaN(result)) {
                    operand1 = result;
                    operator = newOperator;
                    currentInput.setLength(0);
                    operatorPressed = true;
                    textViewHistory.setText("");
                    updateMainDisplayExpression();
                }
            } else {
                operator = newOperator;
                operatorPressed = true;
                updateMainDisplayExpression();
            }
        }
    }

    private void handleEqualsClick() {
        if (Double.isNaN(operand1) || operator.isEmpty() || currentInput.length() == 0 || operatorPressed) {
            return;
        }

        double result = performCalculation();

        if (!Double.isNaN(result)) {
            String op1Str = decimalFormat.format(operand1);
            String op2Str = decimalFormat.format(operand2);

            textViewHistory.setText(op1Str + operator + op2Str);
            textViewHistory.setTextColor(colorGray);

            textViewDisplay.setText(decimalFormat.format(result));
            textViewDisplay.setTextColor(colorGray);

            operand1 = result;
            operator = "";
            currentInput.setLength(0);
            calculationDone = true;
            operatorPressed = false;
        }
    }

    private double performCalculation() {
        if (currentInput.length() == 0) {
            showError("Missing second operand");
            return Double.NaN;
        }
        try {
            operand2 = Double.parseDouble(currentInput.toString());
        } catch (NumberFormatException e) {
            showError("Invalid number format");
            return Double.NaN;
        }

        double result = Double.NaN;
        switch (operator) {
            case "+": result = operand1 + operand2; break;
            case "-": result = operand1 - operand2; break;
            case "×": result = operand1 * operand2; break;
            case "÷":
                if (operand2 == 0) {
                    showError("Division by zero");
                    return Double.NaN;
                }
                result = operand1 / operand2;
                break;
            default:
                showError("Unknown operator");
                return Double.NaN;
        }

        if (Double.isNaN(result) || Double.isInfinite(result)) {
            showError("Result is undefined");
            return Double.NaN;
        }

        return result;
    }

    private void updateMainDisplayExpression() {
        StringBuilder expression = new StringBuilder();

        if (!Double.isNaN(operand1)) {
            expression.append(decimalFormat.format(operand1));
            if (!operator.isEmpty()) {
                expression.append(operator);
                if (!operatorPressed && currentInput.length() > 0) {
                    expression.append(currentInput);
                }
            }
        } else {
            expression.append(currentInput);
        }

        if (expression.length() == 0) {
            textViewDisplay.setText("0");
        } else {
            textViewDisplay.setText(expression.toString());
        }
        textViewDisplay.setTextColor(colorBlack);
    }

    private void clearCalculator() {
        currentInput.setLength(0);
        operand1 = Double.NaN;
        operand2 = Double.NaN;
        operator = "";
        operatorPressed = false;
        calculationDone = false;
        textViewHistory.setText("");
        textViewDisplay.setText("0");
        textViewDisplay.setTextColor(colorBlack);
    }

    private void showError(String message) {
        textViewDisplay.setText("Error");
        textViewDisplay.setTextColor(colorGray);
        textViewHistory.setText("");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        currentInput.setLength(0);
        operand1 = Double.NaN;
        operand2 = Double.NaN;
        operator = "";
        operatorPressed = false;
        calculationDone = true;
    }
}