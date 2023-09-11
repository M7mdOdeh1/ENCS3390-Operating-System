package application;

//Mohammed Owda 1200089
//Mohammad Abu shams 1200549

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main extends Application {

	// Timer timer;
	LinkedList<Process> Q1;
	LinkedList<Process> Q2;
	LinkedList<Process> Q3;
	LinkedList<Process> Q4;
	LinkedList<Process> waitingQueue;
	LinkedList<Process> finishedQueue;

	final int delay = 50; //1000 ms
	// Text txt = new Text();

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) {
		try {
			// generateWorkload(3, 6, 3, 3, 8, 10, 10);

			Q1 = new LinkedList<>();
			Q2 = new LinkedList<>();
			Q3 = new LinkedList<>();
			Q4 = new LinkedList<>();
			// Process currentProcess;

			waitingQueue = new LinkedList<>();
			finishedQueue = new LinkedList<>();

			LinkedList<Process> processQueue = new LinkedList<>();

			// read work load generator file and load it in processQueue list
			readWorkLoadFile(processQueue);
			int noOfProcesses = processQueue.size();

			LinkedList<Integer[]> gantChart = new LinkedList<Integer[]>();
			String gantChartStr="";

			// sort the processQueue list ascending according to arrival time
			Collections.sort(processQueue);

			// Create a timer object which create a thread to schedule a task

			Timer schedulerTimer = new Timer();

			int[] realTimeCounter1 = { 0 };
			int[] counter1 = { 0 };

			Text timerLabel = new Text("00:00:00");

			// Create a timer that updates the label every second
			Timer timer = new Timer();
			Button startBtn = new Button("Start the simulation");

			HBox h1 = new HBox();
			TextField tf1 = new TextField();
			Label lb1 = new Label("q1");
			h1.setAlignment(Pos.CENTER);
			h1.setSpacing(10);
			h1.getChildren().addAll(lb1, tf1);

			tf1.setPrefColumnCount(3);

			HBox h2 = new HBox();
			TextField tf2 = new TextField();
			Label lb2 = new Label("q2");
			h2.setAlignment(Pos.CENTER);
			h2.setSpacing(10);
			h2.getChildren().addAll(lb2, tf2);
			tf2.setPrefColumnCount(3);

			HBox h3 = new HBox();
			TextField tf3 = new TextField();
			Label lb3 = new Label("alpha");
			tf3.setPrefColumnCount(3);
			h3.setAlignment(Pos.CENTER);
			h3.setSpacing(10);
			h3.getChildren().addAll(lb3, tf3);
			tf3.setText("0.5");

			HBox h4 = new HBox();
			TextField tf4 = new TextField();
			tf4.setPrefColumnCount(3);
			Label lb4 = new Label("quanta");
			h4.setAlignment(Pos.CENTER);
			h4.setSpacing(10);
			h4.getChildren().addAll(lb4, tf4);
			tf4.setText("10");

			startBtn.setOnAction(e -> {
				timerLabel.setText("00:00:00");

				int q1 = Integer.parseInt(tf1.getText());
				int q2 = Integer.parseInt(tf2.getText());
				int quanta = Integer.parseInt(tf4.getText());
				double alpha = Double.parseDouble(tf3.getText());

				startBtn.setDisable(true);

				// update the timer label every second
				timer.scheduleAtFixedRate(new TimerTask() {
					int seconds = 0, minutes = 0, hours = 0;

					@Override
					public void run() {
						Platform.runLater(() -> {
							seconds++;
							if (seconds == 60) {
								seconds = 0;
								minutes++;
								if (minutes == 60) {
									minutes = 0;
									hours++;
								}
							}
							timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
						});
					}
				}, 0, delay); // update every second

				//////////////////////////////////////////////////////////////////////////////////////////////
				// The task scheduled to run every 1000 milliseconds
				// timer for Checking arriving Processes, 4 queues, and IO queue
				schedulerTimer.scheduleAtFixedRate(new TimerTask() {
					int counter = 0;
					int realTimeCounter = 0;
					int counterCPUWork = 0;

					@Override
					public void run() {

						// check for arriving processes
						if (!processQueue.isEmpty()) {
							LinkedList<Process> removeList = new LinkedList<>();
							;
							for (int i = 0; i < processQueue.size(); i++) {
								if (processQueue.get(i).getArrivalTime() <= realTimeCounter) {
									// put the process in q1
									processQueue.get(i).status = 2; // ready
									processQueue.element().setCurrentQ(1);
									Q1.offer(processQueue.get(i));
									removeList.add(processQueue.get(i));

								}
							}
							removeList.forEach(p -> {
								processQueue.remove(p);
							});

						}
						//////////////////////////////////////////////////////////////////////////////////////////////

						// Queue 1
						if (!Q1.isEmpty()) {
							// stop running processes in Q2
							if (!Q2.isEmpty()) {
								Q2.element().setStatus(2); // set status to ready
							}
							// stop running processes in Q3
							if (!Q3.isEmpty()) {
								Q3.element().setStatus(2); // set status to ready
							}
							// stop running processes in Q4
							if (!Q4.isEmpty()) {
								Q4.element().setStatus(2); // set status to ready
							}

							// check if the process is ready
							if (Q1.element().getStatus() == 2) {

								Q1.element().setStatus(0); // set status to running
								Q1.element().setCurrentQ(1);
								Q1.element().cpuBurstTime.set(0, Q1.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst in 1 second
								if (Q1.element().cpuBurstTime.getFirst() <= 0) {
									Q1.element().cpuBurstTime.poll();

									Q1.element().setNoOfPreemptionQ1(0);

									// check if there is available IO Bursts
									if (Q1.element().ioBurstTime.isEmpty()) {
										Q1.element().status = 3; // finished
										Q1.element().setTurnAroundTime(realTimeCounter - Q1.element().getArrivalTime());
										// gantChart.add(new Integer[]{Q1.element().getId(), realTimeCounter, 0});
										finishedQueue.add(Q1.poll());
										counter = 0;

									} else {
										Q1.element().status = 1; // Waiting for IO
										waitingQueue.add(Q1.poll());
										counter = 0;
									}
								}

							} else if (Q1.element().getStatus() == 0 && counter < q1) {

								Q1.element().cpuBurstTime.set(0, Q1.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst
								if (Q1.element().cpuBurstTime.getFirst() <= 0) {
									Q1.element().cpuBurstTime.poll();
									Q1.element().setNoOfPreemptionQ1(0);

									// check if there is available IO Bursts
									if (Q1.element().ioBurstTime.isEmpty()) {
										Q1.element().status = 3; // finished
										Q1.element().setTurnAroundTime(realTimeCounter - Q1.element().getArrivalTime());
										finishedQueue.add(Q1.poll());
										counter = 0;

									} else {
										Q1.element().status = 1; // Waiting for IO
										waitingQueue.add(Q1.poll());
										counter = 0;
									}
								}

							}
							// check if the process doesn't finish its burst within q1 time
							else if (Q1.element().getStatus() == 0 && counter >= q1) {
								Q1.element().cpuBurstTime.set(0, Q1.element().cpuBurstTime.getFirst() - 1);
								Q1.element().setStatus(2);// set ready
								// Q1.element().stopCpuBurst();
								int noOfPreemption = Q1.element().getNoOfPreemptionQ1();
								Q1.element().setNoOfPreemptionQ1(noOfPreemption + 1);

								// check if the process does not finish its CPU burst within 10 time-quanta
								if (Q1.element().getNoOfPreemptionQ1() == quanta) {
									Q1.element().setCurrentQ(2);
									Q2.offer(Q1.poll());
								} else {
									Process temp = Q1.poll();
									Q1.offer(temp);
								}

								counter = 0;
							}
							// System.out.println("PID = " + Q1.element().getId() +" :
							// "+Q1.element().getNoOfPreemptionQ1());
							printInfoOfQueues(realTimeCounter, counter);
							counterCPUWork++;
							counter++;
						}
						//////////////////////////////////////////////////////////////////////////////////////////////
						// Queue 2
						else if (!Q2.isEmpty()) {
							// stop running processes in Q3
							if (!Q3.isEmpty()) {
								Q3.element().setStatus(2);
							}
							// stop running processes in Q4
							if (!Q4.isEmpty()) {
								Q4.element().setStatus(2);
							}
							// check if the process is ready
							if (Q2.element().getStatus() == 2) {
								Q2.element().setStatus(0); // set status to running
							//	Q2.element().setNoOfPreemptionQ2(0);
								Q2.element().setCurrentQ(2);

								Q2.element().cpuBurstTime.set(0, Q2.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst in 1 second
								if (Q2.element().cpuBurstTime.getFirst() <= 0) {
									Q2.element().cpuBurstTime.poll();
									Q2.element().setNoOfPreemptionQ2(0);

									// check if there is available IO Bursts
									if (Q2.element().ioBurstTime.isEmpty()) {
										Q2.element().status = 3; // finished
										Q2.element().setTurnAroundTime(realTimeCounter - Q2.element().getArrivalTime());
										finishedQueue.add(Q2.poll());
										counter = 0;
									} else {
										Q2.element().status = 1; // Waiting for IO
										waitingQueue.add(Q2.poll());
										counter = 0;
									}
								}

							} else if (Q2.element().getStatus() == 0 && counter < q2) {
								Q2.element().cpuBurstTime.set(0, Q2.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst
								if (Q2.element().cpuBurstTime.getFirst() <= 0) {
									Q2.element().cpuBurstTime.poll();
									Q2.element().setNoOfPreemptionQ2(0);

									// check if there is available IO Bursts
									if (Q2.element().ioBurstTime.isEmpty()) {
										Q2.element().status = 3; // finished
										Q2.element().setTurnAroundTime(realTimeCounter - Q2.element().getArrivalTime());
										finishedQueue.add(Q2.poll());
										counter = 0;
									} else {
										Q2.element().status = 1; // Waiting for IO
										Q2.element().setCurrentQ(2);
										waitingQueue.add(Q2.poll());
										counter = 0;
									}
								}
							}
							// check if the process doesn't finish its burst within q1 time
							else if (Q2.element().getStatus() == 0 && counter >= q2) {
								Q2.element().cpuBurstTime.set(0, Q2.element().cpuBurstTime.getFirst() - 1);

								Q2.element().setStatus(2);// set ready
								// Q1.element().stopCpuBurst();
								int noOfpreemption = Q2.element().getNoOfPreemptionQ2();
								Q2.element().setNoOfPreemptionQ2(noOfpreemption + 1);
								//System.out.println("Q2: "+Q2.element().getNoOfPreemptionQ2());

								if (Q2.element().getNoOfPreemptionQ2() == quanta) {
									Q2.element().setCurrentQ(3);
									Q3.offer(Q2.poll());
								} else {
									Process temp = Q2.poll();
									Q2.offer(temp);
								}

								counter = 0;
							}

							printInfoOfQueues(realTimeCounter, counter);
							counter++;
							counterCPUWork++;
							//////////////////////////////////////////////////////////////////////////////////////////////
							// Queue 3
						} else if (!Q3.isEmpty()) {
							// stop running processes in Q4
							if (!Q4.isEmpty()) {
								Q4.element().setStatus(2);
							}
							for (Process process : Q3) {
								// (alpha * previous burst time) + ((1 - alpha) * previous predicted burst time)
								process.setPredictOfNextBurst((alpha * process.getPreviousCpuBurst())
										+ ((1 - alpha) * process.getPreviousPredict()));

								process.setPreviousPredict(process.getPredictOfNextBurst());
							}
							Process p = Q3.element();
							Collections.sort(Q3, new SortByPredictOfNextBurst());
							// check if the the process preempted
							if (!p.equals(Q3.element())) {
								p.setNoOfPreemptionQ3(p.getNoOfPreemptionQ3() + 1);
								p.setStatus(2);
							}
							if (p.noOfPreemptionQ3 == 3) {
								Q4.offer(p);
								p.setCurrentQ(4);
								Q3.remove(p);
							}

							// check if the process is ready
							if (Q3.element().getStatus() == 2) {
								counter = 0;
								Q3.element().setStatus(0); // set status to running
								Q3.element().cpuBurstTime.set(0, Q3.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst
								if (Q3.element().cpuBurstTime.getFirst() <= 0) {
									Q3.element().cpuBurstTime.poll();
									// Q3.element().setNoOfPreemptionQ3(0);

									// check if there is available IO Bursts
									if (Q3.element().ioBurstTime.isEmpty()) {
										Q3.element().status = 3; // finished
										Q3.element().setTurnAroundTime(realTimeCounter - Q3.element().getArrivalTime());
										finishedQueue.add(Q3.poll());
										counter = 0;
									} else {
										Q3.element().status = 1; // Waiting for IO
										waitingQueue.add(Q3.poll());
										counter = 0;
									}
								}

							} else if (Q3.element().getStatus() == 1) {// check if the process finish its burst and
																		// waiting for IO
								waitingQueue.add(Q3.poll());
								counter = 0;

							} else if (Q3.element().getStatus() == 3) {// check if the process finished all its bursts
								// waiting for IO
								Q3.element().setTurnAroundTime(realTimeCounter - Q3.element().getArrivalTime());
								finishedQueue.add(Q3.poll());
								counter = 0;

							}
							// check if the process is in running status
							else if (Q3.element().getStatus() == 0) {
								Q3.element().cpuBurstTime.set(0, Q3.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst
								if (Q3.element().cpuBurstTime.getFirst() <= 0) {
									Q3.element().cpuBurstTime.poll();
									// Q3.element().setNoOfPreemptionQ3(0);

									// check if there is available IO Bursts
									if (Q3.element().ioBurstTime.isEmpty()) {
										Q3.element().status = 3; // finished
										Q3.element().setTurnAroundTime(realTimeCounter - Q3.element().getArrivalTime());
										finishedQueue.add(Q3.poll());
										counter = 0;
									} else {
										Q3.element().status = 1; // Waiting for IO
										Q3.element().setCurrentQ(3);
										waitingQueue.add(Q3.poll());
										counter = 0;
									}
								}
								counter = 0;
							}

							printInfoOfQueues(realTimeCounter, counter);
							counter++;
							counterCPUWork++;

						}
						//////////////////////////////////////////////////////////////////////////////////////////////
						// Queue 4
						else if (!Q4.isEmpty()) {
							// check if the process is ready
							if (Q4.element().getStatus() == 2) {
								counter = 0;
								Q4.element().setCurrentQ(4);
								Q4.element().setStatus(0); // set status to running
								// Q4.element().startCpuBurst();
								Q4.element().setCurrentQ(4);

								Q4.element().cpuBurstTime.set(0, Q4.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst in 1 second
								if (Q4.element().cpuBurstTime.getFirst() <= 0) {
									Q4.element().cpuBurstTime.poll();

									// check if there is available IO Bursts
									if (Q4.element().ioBurstTime.isEmpty()) {
										Q4.element().status = 3; // finished
										Q4.element().setTurnAroundTime(realTimeCounter - Q4.element().getArrivalTime());
										finishedQueue.add(Q4.poll());
										counter = 0;
									} else {
										Q4.element().status = 1; // Waiting for IO
										waitingQueue.add(Q4.poll());
										counter = 0;
									}
								}

							} else if (Q4.element().getStatus() == 1) {// check if the process finish its burst and
																		// waiting for IO
								waitingQueue.add(Q4.poll());
								counter = 0;

							} else if (Q4.element().getStatus() == 3) {// check if the process finished all its bursts
								Q4.element().setTurnAroundTime(realTimeCounter - Q4.element().getArrivalTime());
								finishedQueue.add(Q4.poll());
								counter = 0;

								// check if the process is still running
							} else if (Q4.element().getStatus() == 0) {
								// decrement cpuBurstTime
								Q4.element().cpuBurstTime.set(0, Q4.element().cpuBurstTime.getFirst() - 1);

								// check if the process finish the current CPU Burst
								if (Q4.element().cpuBurstTime.getFirst() <= 0) {
									Q4.element().cpuBurstTime.poll();

									// check if there is available IO Bursts
									if (Q4.element().ioBurstTime.isEmpty()) {
										Q4.element().status = 3; // finished
										Q4.element().setTurnAroundTime(realTimeCounter - Q4.element().getArrivalTime());
										finishedQueue.add(Q4.poll());
										counter = 0;
									} else {
										Q4.element().status = 1; // Waiting for IO
										waitingQueue.add(Q4.poll());
										counter = 0;
									}
								}

							}

							printInfoOfQueues(realTimeCounter, counter);
							counter++;
							counterCPUWork++;

							//////////////////////////////////////////////////////////////////////////////////////////////
							// if there is no process in the queues
						} else {
							printInfoOfQueues(realTimeCounter, counter);
							// check if all process is finished
							if (finishedQueue.size() == noOfProcesses) {
								timer.cancel();
								System.out.println("Simulation Ended");
								System.out.println(
										"=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
								double avgWaiting = 0;
								int sumBurst = 0;
								double sumWaiting = 0;
								for (Process process : finishedQueue) {
									process.setWaitingTime(process.getTurnAroundTime() - process.sumOfCPUBursts
											- process.sumOfIOBursts);
									sumWaiting += process.getWaitingTime();
									sumBurst += process.sumOfCPUBursts;
								}
								avgWaiting = sumWaiting / (double) finishedQueue.size();
								System.out.println("AVG Waiting : " + avgWaiting);
								System.out.println("CPU Utilization :" + (sumBurst / (double) realTimeCounter));

								for (int i = 0; i < gantChart.size() - 1; i++) {
									Integer[] n1 = gantChart.get(i);
									Integer[] n2 = gantChart.get(i + 1);

									if (n1[0] == n2[0] && n1[3]==n2[3]) {
										n2[1] = n1[1];
									} else {
										if (n1[0] == -1) {
											System.out.print("--(" + n1[1] + ")--IDLE--(" + n1[2] + ")");

										} else
											System.out.print("--(" + n1[1] + ")--P" + n1[0] +"(Q"+n1[3]+")"+
													"--(" + n1[2] + ")");
									}
								}
								Integer[] n1 = gantChart.get(gantChart.size() - 1);
								if (n1[0] == -1) {
									System.out.print("--(" + n1[1] + ")--IDLE--(" + n1[2] + ")");

								} else {

									System.out.print("--(" + n1[1] + ")--P" + n1[0] +"(Q"+n1[3]+")"+
											"--(" + n1[2] + ")");
								}

								schedulerTimer.cancel();
							}
							// counter++;
						}

						// IO waiting Queue
						if (!waitingQueue.isEmpty()) {
							LinkedList<Process> removeList = new LinkedList<Process>();
							for (Process process : waitingQueue) {
								process.ioBurstTime.set(0, process.ioBurstTime.getFirst() - 1);
								if (process.ioBurstTime.getFirst() <= 0) {
									process.ioBurstTime.poll();
									process.setStatus(2);
									removeList.add(process);
								}
							}

							// remove the process that finished its IO from waittingQueue
							removeList.forEach(p -> {
								if (p.getCurrentQ() == 1)
									Q1.offer(p);
								else if (p.getCurrentQ() == 2)
									Q2.offer(p);
								else if (p.getCurrentQ() == 3)
									Q3.offer(p);
								else if (p.getCurrentQ() == 4)
									Q4.offer(p);

								waitingQueue.remove(p);
							});

						}
						realTimeCounter++;

						realTimeCounter1[0] = realTimeCounter;
						counter1[0] = counter;
						// counter++;

					}
				}, 0, delay);

				
				//add intervals for gant chart
				schedulerTimer.scheduleAtFixedRate(new TimerTask() {
					int counter = 0;

					@Override
					public void run() {

						Process p = null;

						if (!Q1.isEmpty()) {
							p = Q1.element();
						} else if (!Q2.isEmpty()) {
							p = Q2.element();
						} else if (!Q3.isEmpty()) {
							p = Q3.element();
						} else if (!Q4.isEmpty()) {
							p = Q4.element();
						}

						if (p != null) {
							gantChart.add(new Integer[] { p.getId(), counter, counter + 1, p.getCurrentQ() });

						} else {
							gantChart.add(new Integer[] { -1, counter, counter + 1, -1});
						}
						counter++;

					}
				}, 0, delay);
			});

			// threads for Processes waiting for IO
			/*
			 * schedulerTimer.scheduleAtFixedRate(new TimerTask() { // int counter = 0;
			 * 
			 * @Override public void run() {
			 * 
			 * // check if the there is process arrived to waitingQueue if
			 * (!waitingQueue.isEmpty()) { LinkedList<Process> removeList = new
			 * LinkedList<Process>(); for (Process process : waitingQueue) {
			 * process.ioBurstTime.set(0, process.ioBurstTime.getFirst() - 1); if
			 * (process.ioBurstTime.getFirst() <= 0) { process.ioBurstTime.poll();
			 * process.setStatus(2); removeList.add(process); } }
			 * 
			 * // remove the process that finished its IO from waittingQueue
			 * removeList.forEach(p -> { if (p.getCurrentQ() == 1) Q1.offer(p); else if
			 * (p.getCurrentQ() == 2) Q2.offer(p); else if (p.getCurrentQ() == 3)
			 * Q3.offer(p); else if (p.getCurrentQ() == 4) Q4.offer(p);
			 * 
			 * waitingQueue.remove(p); });
			 * 
			 * }
			 * 
			 * } }, 0, delay);
			 */

			Button btn = new Button("Print the current Result");
			TextArea ta = new TextArea();

			// print info's button
			btn.setOnAction(e -> {
				// printInfoOfQueues(realTimeCounter1[0], counter1[0]);
				ta.setText("realTime: " + realTimeCounter1[0]);
				ta.appendText("\nTimer2: " + counter1[0]);
				ta.appendText("\nQ1: " + Q1);
				ta.appendText("\nQ2: " + Q2);
				ta.appendText("\nQ3: " + Q3);
				ta.appendText("\nQ4: " + Q4);
				ta.appendText("-------------------------------");
				ta.appendText("\nwatingQ: " + waitingQueue);
				ta.appendText("\nfinishedQ: " + finishedQueue);

				if (!Q1.isEmpty() && Q1.element().getStatus() == 0) {
					ta.appendText("\nRunning Process (Q1): " + Q1.element());
				} else if (!Q2.isEmpty() && Q2.element().getStatus() == 0) {
					ta.appendText("\nRunning Process (Q2): " + Q2.element());
				} else if (!Q3.isEmpty() && Q3.element().getStatus() == 0) {
					ta.appendText("\nRunning Process (Q3): " + Q3.element());
				} else if (!Q4.isEmpty() && Q4.element().getStatus() == 0) {
					ta.appendText("\nRunning Process (Q4): " + Q4.element());
				}

			});

			StackPane sp = new StackPane();
			sp.getChildren().add(timerLabel);
			sp.setAlignment(Pos.CENTER);

			VBox vb = new VBox();
			vb.getChildren().addAll(startBtn, btn, ta);
			vb.setAlignment(Pos.CENTER);
			vb.setSpacing(20);

			VBox vb2 = new VBox();
			vb2.getChildren().addAll(h1, h2, h3, h4);
			// vb2.setAlignment(Pos.to);
			vb2.setSpacing(20);

			sp.setPadding(new Insets(0, 70, 0, 0));

			// timerLabel.setal
			// timerLabel.setAlignment(Pos.CENTER);
			timerLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 45));
			timerLabel.setFill(Color.WHITE);
			timerLabel.setStroke(Color.BLACK);
			timerLabel.setStrokeWidth(2);
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 800, 500);
			root.setCenter(vb);
			root.setRight(vb2);
			root.setTop(sp);
			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setAlwaysOnTop(true);
			primaryStage.setTitle("Operatin System Project (MLFQ)");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void printInfoOfQueues(int realTimeCounter, int counter) {
		System.out.println("");
		System.out
				.println("===========================================================================================");
		System.out.println("realTime: " + realTimeCounter);
		System.out.println("Timer2: " + counter);
		System.out.println("Q1: " + Q1);
		System.out.println("Q2: " + Q2);
		System.out.println("Q3: " + Q3);
		System.out.println("Q4: " + Q4);
		System.out.println("-------------------------------");
		System.out.println("watingQ: " + waitingQueue);
		System.out.println("finishedQ: " + finishedQueue);
		System.out
				.println("===========================================================================================");

	}

	public static void readWorkLoadFile(LinkedList<Process> processQueue) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader("workload.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			String[] values = line.split(" ");
			int id = Integer.parseInt(values[0]);
			int arrivalTime = Integer.parseInt(values[1]);
			LinkedList<Integer> cpuBurstTime = new LinkedList<>();
			LinkedList<Integer> ioBurstTime = new LinkedList<>();

			for (int i = 0; i < (values.length - 3) / 2; i++) {
				cpuBurstTime.offer(Integer.parseInt(values[2 + i * 2]));
				ioBurstTime.offer(Integer.parseInt(values[3 + i * 2]));
			}
			cpuBurstTime.offer(Integer.parseInt(values[values.length - 1]));

			processQueue.offer(new Process(id, arrivalTime, cpuBurstTime, ioBurstTime));
		}

	}

	public static void generateWorkload(int numProcesses, int maxArrivalTime, int maxCPUBursts, int minIO, int maxIO,
			int minCPU, int maxCPU) {

		Random rand = new Random();
		try {
			File file = new File("workload.txt");
			FileWriter writer = new FileWriter(file);
			for (int i = 0; i < numProcesses; i++) {
				int arrivalTime = rand.nextInt(maxArrivalTime + 1);
				int numCPUBursts = rand.nextInt(maxCPUBursts) + 1;
				writer.write(i + " " + arrivalTime + " ");
				for (int j = 0; j < numCPUBursts; j++) {
					int cpuDuration = rand.nextInt(maxCPU - minCPU + 1) + minCPU;
					writer.write(cpuDuration + " ");
					if (j < numCPUBursts - 1) {
						int ioDuration = rand.nextInt(maxIO - minIO + 1) + minIO;
						writer.write(ioDuration + " ");
					}
				}
				writer.write("\n");
			}
			System.out.println("Work load Generated!!!");
			writer.close();
		} catch (IOException e) {
			System.out.println("Error generating workload: " + e.getMessage());
		}
	}

}
