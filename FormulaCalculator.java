package com.javacourse2018.lw03.service;

import com.javacourse2018.lw03.model.*;
import com.javacourse2018.lw03.utils.Utilites;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class FormulaCalculatorImpl implements PrefixFormCalculator {

    private final Stack<String> stack = new Stack<>();

    private Operands operands = new Operands();

    private String calculatonResult = "";

    private Spreadsheet spreadsheet;

    public FormulaCalculatorImpl(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public String getCalculationResult() {
        return calculatonResult;
    }

    @Override
    public void calculate(String formula) throws IllegalArgumentException, IndexOutOfBoundsException {
        formula = formula.replaceAll("[\\(\\)]", "");
        List<String> elements = Arrays.asList(formula.split(" "));
        for (int i = elements.size() - 1; i != 0; --i) {
            String item = elements.get(i);
            if (item.equals("")) {
                continue;
            }
            if (!item.contains(Operation.operationToString(Operation.ADDITION))
                    && !item.contains(Operation.operationToString(Operation.SUBTRACTION))
                    && !item.contains(Operation.operationToString(Operation.MULTIPLICATION))
                    && !item.contains(Operation.operationToString(Operation.DIVISION))) {
                this.stack.push(item);
                continue;
            }
            if (item.length() != 1) {
                throw new IllegalArgumentException("Incorrect operation: " + item);
            }
            Operation operation = Operation.createFromCharacter(item.toCharArray()[0]);
            this.expressionEvaluationManager(operation);
        }
        if (this.stack.size() == 1) {
            this.calculatonResult = this.stack.pop();
            return;
        }
        throw new IllegalArgumentException("Incorrect formula." + formula);
    }

    @Override
    public void clearResult() {
        this.operands.clear();
        this.calculatonResult = "";
        this.stack.clear();
    }

    private void expressionEvaluationManager(Operation operation) throws IllegalArgumentException, IndexOutOfBoundsException {
        this.operands.clear();
        switch (operation) {
            case ADDITION:
                this.additionHandler();
                break;
            case SUBTRACTION:
                this.subtractionHandler();
                break;
            case MULTIPLICATION:
                this.multiplicationHandler();
                break;
            case DIVISION:
                this.divisionHandler();
                break;
            default:
                break;
        }

    }

    private void divisionHandler() throws IllegalArgumentException {
        this.handleOperands(false);
        if ((!this.operands.getFirst().equals(this.operands.zero())
                && !this.operands.getSecond().equals(this.operands.zero()))) {
            stack.push(Double.toString(this.operands.getFirst() / this.operands.getSecond()));
        } else {
            throw new IllegalArgumentException("Error in the formula.");
        }
    }

    private void multiplicationHandler() throws IllegalArgumentException {
        this.handleOperands(false);
        if ((!this.operands.getFirst().equals(this.operands.zero())
                && !this.operands.getSecond().equals(this.operands.zero()))) {
            stack.push(Double.toString(this.operands.getFirst() * this.operands.getSecond()));
        } else {
            throw new IllegalArgumentException("Error in the formula.");
        }
    }

    private void subtractionHandler() throws IllegalArgumentException {
        this.handleOperands(false);
        if ((!this.operands.getFirst().equals(this.operands.zero())
                && !this.operands.getSecond().equals(this.operands.zero()))) {
            stack.push(Double.toString(this.operands.getFirst() - this.operands.getSecond()));
        } else {
            throw new IllegalArgumentException("Error in the formula.");
        }
    }

    private void additionHandler() {
        this.handleOperands(true);
        if ((!this.operands.getFirst().equals(this.operands.zero())
                && !this.operands.getSecond().equals(this.operands.zero()))) {
            stack.push(Double.toString(this.operands.getFirst() + this.operands.getSecond()));
            return;
        }
        stack.push(getTheSumOfTwoOperands());
    }

    private void handleOperands(Boolean isSum) {
        this.operands.setFirstStr(stack.pop());
        this.operands.setSecondStr(stack.pop());
        this.checkOperandsForNumbers();
        if (Command.isValidCoordinate(this.operands.getFirstStr())) {
            this.handleFirstCoordinate(isSum);
        }
        if (Command.isValidCoordinate(this.operands.getSecondStr())) {
            this.handleSecondCoordinate(isSum);
        }
    }

    private String getTheSumOfTwoOperands() {
        String result = "";
        if (this.operands.getSecond().equals(this.operands.zero())
                && this.operands.getFirst().equals(this.operands.zero())) {
            result = this.operands.getFirstStr() + this.operands.getSecondStr() ;
        } else {
            if (this.operands.getFirst().equals(this.operands.zero())) {
                result = this.operands.getFirstStr() + this.operands.getSecond().toString();
            } else if (this.operands.getSecond().equals(this.operands.zero())) {
                result = this.operands.getFirst().toString() + this.operands.getSecondStr();
            } else {
                result = this.operands.getFirstStr() + this.operands.getSecondStr();
            }
        }
        return result;
    }

    //  @param class fields
    private void checkOperandsForNumbers() {
        if (Utilites.isNumber(this.operands.getFirstStr())) {
            this.operands.setFirst(Double.parseDouble(this.operands.getFirstStr()));
        }
        if (Utilites.isNumber(this.operands.getSecondStr())) {
            this.operands.setSecond(Double.parseDouble(this.operands.getSecondStr()));
        }
    }

    //  @param class fields
    private void handleFirstCoordinate(Boolean isSum) throws IllegalArgumentException {
        String number = this.operands.getFirstStr().substring(1, this.operands.getFirstStr().length());
        Position position = new Position(this.operands.getFirstStr().toUpperCase().charAt(0), Integer.parseInt(number));
        Cell cell;
        cell = this.spreadsheet.getCell(position);
        String cellValue = cell.getValue();
        if (Utilites.isNumber(cellValue)) {
            this.operands.setFirst(Double.parseDouble(cell.getValue()));
        } else {
            if (isSum) {
                this.operands.setFirstStr(cellValue);
            } else {
                throw new IllegalArgumentException("Error in the formula.");
            }
        }
        Position pos = cell.getPosition();
        if (!this.spreadsheet.getCellsIncludedInTheFormula().contains(pos)) {
            this.spreadsheet.addPositionToCellsIncludedInTheFormula(pos);
        }
    }

    private void handleSecondCoordinate(Boolean isSum) throws IllegalArgumentException {
        String number = this.operands.getSecondStr().substring(1, this.operands.getSecondStr().length());
        Position position = new Position(this.operands.getSecondStr().toUpperCase().charAt(0), Integer.parseInt(number));
        Cell cell;
        cell = this.spreadsheet.getCell(position);
        String cellValue = cell.getValue();
        if (Utilites.isNumber(cellValue)) {
            this.operands.setSecond(Double.parseDouble(cell.getValue()));
        } else {
            if (isSum) {
                this.operands.setSecondStr(cellValue);
            } else {
                throw new IllegalArgumentException("Error in the formula.");
            }
        }
        Position pos = cell.getPosition();
        if (!this.spreadsheet.getCellsIncludedInTheFormula().contains(pos)) {
            this.spreadsheet.addPositionToCellsIncludedInTheFormula(pos);
        }
    }

}