package com.chocolateam.galileopvt;

import Jama.Matrix;

/* Refer to http://en.wikipedia.org/wiki/Kalman_filter for
 mathematical details. The naming scheme is that variables get names
 that make sense, and are commented with their analog in
 the Wikipedia mathematical notation.
 This Kalman filter implementation does not support controlled
 input.
 (Like knowing which way the steering wheel in a car is turned and
 using that to inform the model.)
 Vectors are handled as n-by-1 matrices.
 TODO: comment on the dimension of the matrices */

public class KalmanFilter {
	/* k */
	int timestep;

	/* These parameters define the size of the matrices. */
	int state_dimension, observation_dimension;

	/* This group of matrices must be specified by the user. */
	/* F_k */
	Matrix state_transition;
	/* H_k */
	Matrix observation_model;
	/* Q_k */
	Matrix process_noise_covariance;
	/* R_k */
	Matrix observation_noise_covariance;

	/* The observation is modified by the user before every time step. */
	/* z_k */
	Matrix observation;

	/* This group of matrices are updated every time step by the filter. */
	/* x-hat_k|k-1 */
	Matrix predicted_state;
	/* P_k|k-1 */
	Matrix predicted_estimate_covariance;
	/* y-tilde_k */
	Matrix innovation;
	/* S_k */
	Matrix innovation_covariance;
	/* S_k^-1 */
	Matrix inverse_innovation_covariance;
	/* K_k */
	Matrix optimal_gain;
	/* x-hat_k|k */
	Matrix state_estimate;
	/* P_k|k */
	Matrix estimate_covariance;

	/* This group is used for meaningless intermediate calculations */
	Matrix vertical_scratch;
	Matrix small_square_scratch;
	Matrix big_square_scratch;

	public KalmanFilter(int state_dimension, int observation_dimension) {
		timestep = 0;
		this.state_dimension = state_dimension;
		this.observation_dimension = observation_dimension;

		state_transition = new Matrix(state_dimension, state_dimension);
		observation_model = new Matrix(observation_dimension, state_dimension);
		process_noise_covariance = new Matrix(state_dimension, state_dimension);
		observation_noise_covariance = new Matrix(observation_dimension, observation_dimension);

		observation = new Matrix(observation_dimension, 1);

		predicted_state = new Matrix(state_dimension, 1);
		predicted_estimate_covariance = new Matrix(state_dimension,	state_dimension);
		innovation = new Matrix(observation_dimension, 1);
		innovation_covariance = new Matrix(observation_dimension, observation_dimension);
		inverse_innovation_covariance = new Matrix(observation_dimension, observation_dimension);
		optimal_gain = new Matrix(state_dimension, observation_dimension);
		state_estimate = new Matrix(state_dimension, 1);
		estimate_covariance = new Matrix(state_dimension, state_dimension);

		vertical_scratch = new Matrix(state_dimension, observation_dimension);
		small_square_scratch = new Matrix(observation_dimension,observation_dimension);
		big_square_scratch = new Matrix(state_dimension, state_dimension);
	}

	/*
	 * Runs one timestep of prediction + estimation.
	 * 
	 * Before each time step of running this, set f.observation to be the next
	 * time step's observation.
	 * 
	 * Before the first step, define the model by setting: f.state_transition
	 * f.observation_model f.process_noise_covariance
	 * f.observation_noise_covariance
	 * 
	 * It is also advisable to initialize with reasonable guesses for
	 * f.state_estimate f.estimate_covariance
	 */
	void update() {
		predict();
		estimate();
	}

	/* Just the prediction phase of update. */
	void predict() {
		timestep++;

		/* Predict the state */
		predicted_state = state_transition.times(state_estimate);

		/* Predict the state estimate covariance */
		big_square_scratch = state_transition.times(estimate_covariance);
		predicted_estimate_covariance = big_square_scratch.times(state_transition.transpose());
		predicted_estimate_covariance = predicted_estimate_covariance.plus(process_noise_covariance);
	}

	/* Just the estimation phase of update. */
	void estimate() {
		/* Calculate innovation */
		innovation = observation_model.times(predicted_state);
		innovation = observation.minus(innovation);

		/* Calculate innovation covariance */
		vertical_scratch = predicted_estimate_covariance.times(observation_model.transpose());
		innovation_covariance = observation_model.times(vertical_scratch);
		innovation_covariance = innovation_covariance.plus(observation_noise_covariance);

		/*
		 * Invert the innovation covariance. Note: this destroys the innovation
		 * covariance. TODO: handle inversion failure intelligently.
		 */
		inverse_innovation_covariance = innovation_covariance.inverse();

		/*
		 * Calculate the optimal Kalman gain. Note we still have a useful
		 * partial product in vertical scratch from the innovation covariance.
		 */
		optimal_gain = vertical_scratch.times(inverse_innovation_covariance);

		/* Estimate the state */
		state_estimate = optimal_gain.times(innovation);
		state_estimate = state_estimate.plus(predicted_state);

		/* Estimate the state covariance */
		big_square_scratch = optimal_gain.times(observation_model);
		big_square_scratch = Matrix.identity(big_square_scratch.getRowDimension(), big_square_scratch.getColumnDimension()).minus(big_square_scratch);
		estimate_covariance = big_square_scratch.times(predicted_estimate_covariance);
	}
}
