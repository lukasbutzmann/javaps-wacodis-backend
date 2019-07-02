/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps;

import com.bc.ceres.core.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class LoggerProgressMonitor implements ProgressMonitor {

    private final Logger LOGGER;

    private boolean canceled;
    private String taskName;
    private String subTaskName;
    private double totalWork;
    private double currentWork;
    private int printMinorStepPercentage;
    private int printStepPercentage;
    private int percentageWorked;
    private int lastMinorPercentagePrinted;
    private int lastPercentagePrinted;

    public LoggerProgressMonitor() {
        this(LoggerFactory.getLogger(LoggerProgressMonitor.class));
    }

    public LoggerProgressMonitor(Logger logger) {
        this.LOGGER = logger;
        printMinorStepPercentage = 2; // = 2%
        printStepPercentage = 10; // =10%
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    @Override
    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }

    public int getPrintMinorStepPercentage() {
        return printMinorStepPercentage;
    }

    public int getPrintStepPercentage() {
        return printStepPercentage;
    }

    public void setPrintMinorStepPercentage(int printMinorStepPercentage) {
        this.printMinorStepPercentage = printMinorStepPercentage;
    }

    public void setPrintStepPercentage(int printStepPercentage) {
        this.printStepPercentage = printStepPercentage;
    }

    public int getPercentageWorked() {
        return percentageWorked;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
        if (isCanceled()) {
            printCanceledMessage();
        }
    }

    @Override
    public void internalWorked(double work) {
        currentWork += work;
        percentageWorked = (int) (100.0 * currentWork / totalWork);
        if (percentageWorked - lastMinorPercentagePrinted >= getPrintMinorStepPercentage()) {
            lastMinorPercentagePrinted = percentageWorked;
            if (percentageWorked - lastPercentagePrinted >= getPrintStepPercentage()) {
                printWorkedMessage();
                lastPercentagePrinted = percentageWorked;
            } else {
                printMinorWorkedMessage();
            }
        }
    }

    @Override
    public void done() {
        printDoneMessage();
    }

    @Override
    public void beginTask(String taskName, int totalWork) {
        this.taskName = taskName;
        this.totalWork = totalWork;
        currentWork = 0.0;
        percentageWorked = 0;
        lastMinorPercentagePrinted = 0;
        lastPercentagePrinted = 0;
        canceled = false;
    }

    @Override
    public void worked(int work) {
        internalWorked(work);
    }

    protected void printStartMessage() {
        LOGGER.debug("{}, started\n", getMessage());
    }

    protected void printWorkedMessage() {
        LOGGER.debug("{}, {}% worked", getMessage(), getPercentageWorked());
    }

    protected void printMinorWorkedMessage() {
    }

    protected void printDoneMessage() {
        LOGGER.debug("{}, done", getMessage());
    }

    protected void printCanceledMessage() {
        LOGGER.debug("{}, cancelation requested", getMessage());
    }

    protected String getMessage() {
        boolean validTaskName = taskName != null && taskName.length() > 0;
        boolean validSubTaskName = subTaskName != null && subTaskName.length() > 0;
        String message = "";
        if (validTaskName && validSubTaskName) {
            message = taskName + " - " + subTaskName;
        } else if (validTaskName) {
            message = taskName;
        } else if (validSubTaskName) {
            message = subTaskName;
        }
        return message;
    }

}
