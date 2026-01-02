import java.util.Random;

public class artificialBeeColonyLMG49 {

    // define parameters 
    // dimensions - bounds

    private static final int dimensions = 30;       //define number of dimensions in the algorithm, use final so that it cannot be reassigned
    private static final double lowerBound = -5.12; //define lower bound for what value each variable can take, use final so that it cannot be reassigned
    private static final double upperBound = 5.12;  //define upper bound for what values each variable can take, use final so that it cannot be reassigned

    // ABC parameters
    // colony size, food sources, iterations, limit

    private static final int colonySize = 50;               //total number of bees in the colony
    private static final int foodSources = colonySize / 2;  //number of food sources in the algorithm, equal to the colony size divided by 2
    private static final int maxIterations = 2000;          //how many loops the algorithm runs
    private static final int trialCounterLimit = 100;       //limit for the trial counter before the food source is removed and replaced

    // structures
    // food sources, fitness, trials, best solution, best fitness

    private Random rand;            //random number generator
    private long seed;              //seed used for random number generation to allow for reproduceability, all runs under same seed should produce same result
    private double[][] foodSourcesArray; //2 dimensional array where each row is a different food source

    private double[] targetValue;   //target value for f(x)
    private double[] fitness;       //fitness value for each food source where lower is better
    private int[] trialCounter;     //trial counter
    private double[] bestSolution;  // global best solution found so far
    private double bestTargetValue; //best f(x) found so far
    private double bestFitness;     //fitness of best solution

    // initialisation function

    public artificialBeeColonyLMG49(long seed) {
        this.seed = seed;                              //initialise all the structures made in the previous section
        rand = new Random(seed);
        foodSourcesArray = new double[foodSources][dimensions];
        targetValue = new double[foodSources];
        fitness = new double[foodSources];
        trialCounter = new int[foodSources];
        bestSolution = new double[dimensions];
        bestTargetValue = Double.MAX_VALUE;     //start with worst possible f(x)
        bestFitness = 0.0;                      //start with worst possible fitness
    }

    // generate food source initial population

    private void initialise() {
        for (int i = 0; i < foodSources; i++) {                                                         //generate a random solution within the specified bounds for each food source
            for (int j = 0; j < dimensions; j++) {
                foodSourcesArray[i][j] = lowerBound + rand.nextDouble() * (upperBound - lowerBound);    //this gives a uniform random value within the specified bounds
            }
            
            targetValue[i] = rastrigin(foodSourcesArray[i]);//evaluate f(x) of each created food source
            fitness[i] = calculateFitness(targetValue[i]);  //evaluate the fitness of each created food source
            trialCounter[i] = 0;                            //set the initial trial counter of every source to 0 when it is created

            if (targetValue[i] < bestTargetValue) {                 //update the global best solution if the new one has a higher f(x)
                bestTargetValue = targetValue[i];
                for (int k = 0; k < dimensions; k++) {
                    bestSolution[k] = foodSourcesArray[i][k];
                }
            }
        }
    }

    // fitness calculation

    private double calculateFitness(double targetValue) {
        return 1.0 / (1.0 + targetValue);                   //uses the formula 1/1+f(x) to calculate fitness so that as f(x) approaches 0, fitness tends to 1
    }

    // probability calculation

    private double[] calculateProbabilities() {
        double[] probability = new double[foodSources];

        double maxFitness = Double.MIN_VALUE;       //finds the maximum fitness in the population for to be used in the later calculation probability 
        for (int i = 0; i < foodSources; i++) {
            if (fitness[i] > maxFitness) {
                maxFitness = fitness[i];
            }
        }

        for (int i = 0; i < foodSources; i++) {
            probability[i] = 0.9 * (fitness[i] / maxFitness) + 0.1;     //uses the formula 0.9 * (fitness/maxfitness) + 0.1 to calculate a probability for choosing a food source so the best has a probability of 1
        }

        return probability;
    }

    // employed bee phase function

    private void employedBeePhase() {       //process each food source individually as each employeed bee works on one source
        for (int i = 0; i < foodSources; i++) {
            double[] newSource = new double[dimensions];    //create a copy of the current solution to modify and compare
            System.arraycopy(foodSourcesArray[i], 0, newSource, 0, dimensions);

            int dimensionChoice = rand.nextInt(dimensions);     //randomly select which dimension to modify in the new food source

            int neighbour = rand.nextInt(foodSources);  //select a random neighbour to modify the food source from
            while (neighbour == i) {
                neighbour = rand.nextInt(foodSources);  //a while loop just to make sure we do not randomly pick the food source itself
            }

            double phi = -1 + rand.nextDouble() * 2;    //generates a randomly value in [-1,1] for our phi value
            newSource[dimensionChoice] = foodSourcesArray[i][dimensionChoice] + phi * (foodSourcesArray[i][dimensionChoice] - foodSourcesArray[neighbour][dimensionChoice]);    //new value is based on the current value plus a step towards or away from its chosen neighbour
            newSource[dimensionChoice] = Math.max(lowerBound, Math.min(upperBound, newSource[dimensionChoice]));    //ensures the new value stays within the defined bounds

            double newTargetValue = rastrigin(newSource);           //f(x) of new food source
            double newFitness = calculateFitness(newTargetValue);   //fitness of new food source

            if (newFitness > fitness[i]) {                          //comparing fitness to replace then replace original food source if the new fitness is better
                for (int j = 0; j < dimensions; j++) {
                    foodSourcesArray[i][j] = newSource[j];
                }

                targetValue[i] = newTargetValue;
                fitness[i] = newFitness;
                trialCounter[i] = 0;

                if (newTargetValue < bestTargetValue) {             //comparing new f(x) to global best to replace the global best solution if the new one is an imrpovement 
                    bestTargetValue = newTargetValue;
                    bestFitness = newFitness;
                    for (int k = 0; k < dimensions; k++) {
                        bestSolution[k] = newSource[k];
                    }
                }
            } else {
                trialCounter[i]++;                                  //increasing trial counter if the new food source is not an improvement over the original
            }
        }

    }

    // onlooker bee phase function

    private void onlookerBeePhase() {
        double[] probability = calculateProbabilities();    //get selection probabilities
        int onlookerBees = 0;                               //counter for onlooker bees
        int i = 0;                                          //counter for food sources

        while (onlookerBees < foodSources) {    //deploy one onlooker bee per food source
            if(rand.nextDouble() < probability[i]) {    //if random number is less than the probability then select food source if not move onto the next
                double[] newSource = new double[dimensions];
                System.arraycopy(foodSourcesArray[i], 0, newSource, 0, dimensions);

                int dimensionChoice = rand.nextInt(dimensions); //this now uses the same system as employed bees and therefore has same comments as employeed bees as it does the same
                int neighbour = rand.nextInt(foodSources);
                while (neighbour == 1) {
                    neighbour = rand.nextInt(foodSources);
                }

                double phi = -1 + rand.nextDouble() * 2;    //generates a randomly value in [-1,1] for our phi value
                newSource[dimensionChoice] = foodSourcesArray[i][dimensionChoice] + phi * (foodSourcesArray[i][dimensionChoice] - foodSourcesArray[neighbour][dimensionChoice]);    //new value is based on the current value plus a step towards or away from its chosen neighbour
                newSource[dimensionChoice] = Math.max(lowerBound, Math.min(upperBound, newSource[dimensionChoice]));    //ensures the new value stays within the defined bounds

                double newTargetValue = rastrigin(newSource);               //the following section is the exact same as in employed bee phase so i am not repeating all the comments because its indentical code
                double newFitness = calculateFitness(newTargetValue);

                if (newFitness > fitness[i]) {
                    for (int j = 0; j < dimensions; j++) {
                        foodSourcesArray[i][j] = newSource[j];
                    }

                    targetValue[i] = newTargetValue;
                    fitness[i] = newFitness;
                    trialCounter[i] = 0;

                    if (newTargetValue < bestTargetValue) {
                        bestTargetValue = newTargetValue;
                        bestFitness = newFitness;
                        for (int k = 0; k < dimensions; k++) {
                            bestSolution[k] = newSource[k];
                        }
                    }
                } else {
                    trialCounter[i]++;
                }

                onlookerBees++;
            }

            i = (i +1) % foodSources;       //makes sure that when the source number reaches the maximum it loops back round to the beginning 
        }
    }

    // scout bee phase function

    private void scoutBeePhase() {
        for (int i = 0; i < foodSources; i++) {
            if (trialCounter[i] > trialCounterLimit) {  //check if food source has failed to improve too many times
                for (int j = 0; j< dimensions; j++) {   //abandon food source and generate a completely new one, single source initialisation
                    foodSourcesArray[i][j] = lowerBound + rand.nextDouble() * (upperBound - lowerBound);
                }


            targetValue[i] = rastrigin(foodSourcesArray[i]);    //evaluate new random source
            fitness[i] = calculateFitness(targetValue[i]);
               

            trialCounter[i] = 0;    //reset trial counter 
            }
        }
    }

    // create Rastrigin function

    private double rastrigin(double[] x) {
        double fx = 0;   //base value
        for (int i = 0; i < dimensions; i++) {  //rastrigin equation for each dimension
            fx += (x[i] * x[i] - 10 * Math.cos(2.0 * Math.PI * x[i]) +10);
        }
        return fx;
    }

    // run optimisation

    public void optimise() {
        initialise();   //initialise everything

        for (int iteration = 0; iteration < maxIterations; iteration++) {   //run through all iterations
            
            employedBeePhase(); //run through employed phase
            onlookerBeePhase(); //run through onlooker phase
            scoutBeePhase();    //run through scout phase
            System.out.println("Iteration Number: " + iteration + " -   Best fitness: " + bestFitness); //prints the best global fitness after every iteration 
        }

        for (int i = 0; i < dimensions; i++){
            System.out.println(i + " " + bestSolution[i]);      //prints out all 30 dimensions of the best solution 
        }
        System.out.println("Best fitness: " + bestFitness);     //print best global fitness after all iterations done
        System.out.println("F(x): " + bestTargetValue);
    }

    // gives options about seeds
    // I did it this way to make testing easier so I could very quickly switch between user seeds and random seeds

    private static final boolean inputSeed = true; //gives the option to use a user seed
    private static final long userSeed = -1323374069635216480L;  //enter teh user seed to replicate results

    // main function

    public static void main(String[] args) {
        long seed;  //takes the seed inputed

        if (inputSeed) {
            seed = userSeed;    //runs if user input seed is set to true
        } else {
            Random seedGenerate = new Random(); //randomly generates a seed if no user one is input
            seed = seedGenerate.nextLong();
        }

        System.out.println();

        artificialBeeColonyLMG49 abc = new artificialBeeColonyLMG49(seed);  
        abc.optimise();     //runs the optimisation
        System.out.println("\nSeed used: " + seed); //prints the seed used
    }
}