# Vai
This repository contains a collection of neural network models for the popular first person shooter video game Valorant. The models are designed to analyze statistics of matches and performance of players. This is a personal project and was created for the purposes of learning more about machine learning. 

## Repository Contents
This repository includes models, scripts to collect data using my data retriever and parser, and scripts to put the data into file. All data that was used to train and evaluate the models are also in the repository formatted as CSV files. 

## Data
The data I used to train my models was from [tracker.gg](https://tracker.gg/valorant) using public player accounts and matches. Data was collected from April 2023 to present (September 2023 as of writing). 

## Models
Currently there are 2 models both built with DeepLearning4J and a seperate OCR model using Tesseract. All of these models are rough and unoptimized since this project was just for me to learn more about AI, however the performances of both models suprised me. 

#### Player Winrate Regression
This is a regression model predicting an individual player's overall winrate based off of their past performance. This was my first attempt at creating a neural network and was meant as a first test to figure out the basics of neural networks and thus, as stated beforehand, is very rough. 

The model was trained on about 1500 individual players and the model had an r^2 value of 0.74. 

#### Game Outcome Prediction
A binary classification model that predicts the outcome of a match based on team composition and past player performance. This model was my primary goal and also the hardest to get working. There were many problems with getting the model to work, with my main headaches coming from the data I was using to train the model. Almost all of the matches I was using had missing data, as some player profiles were set to private meaning I was unable to use their past performance. This meant that I could only use less than half of the approximatly 1000 matches I had. 

The training data was balanced so that there was an equal number of defender wins and attacker wins and only included matches that had at least 7 players. The evaluation data was unbalanced and included matches with at least 2 players and at most 6 players. 