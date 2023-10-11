# Vai
This repository contains a collection of neural network models for the popular first-person shooter video game Valorant. The models are designed to analyze statistics of matches and performance of players. This is a personal project and was created for the purpose of learning more about machine learning. 

## Repository Contents
This repository includes models, scripts to collect data using my data retriever and parser, and scripts to put the data into file. All data that was used to train and evaluate the models are also in the repository formatted as CSV files. 

## Data
The data I used to train my models was from [tracker.gg](https://tracker.gg/valorant) using public player accounts and matches. Data was collected from April 2023 to present (September 2023 as of writing). 

## Models
Currently there are 2 models both built with DeepLearning4J and a separate OCR model using Tesseract. Both models are rough and unoptimized since this project was just for me to learn more about AI, however the performances of both models were surprising in my opinion. 

#### Player Winrate Regression
This is a regression model predicting an individual player's overall winrate based off their past performance. This was my first attempt at creating a neural network and was meant as a first test to figure out the basics of neural networks and thus, as stated beforehand, is very rough. 

The model was trained on about 1500 individual players and the model had an r^2 value of 0.74. 

#### Game Outcome Prediction
A binary classification model that predicts the outcome of a match based on team composition and past player performance. This model was my primary goal and the hardest to get working. There were many problems with getting the model to work, with my main headaches coming from the data I was using to train the model. Almost all the matches I was using had missing data, as some player profiles were set to private meaning, I was unable to use their past performance. This meant that I could only use less than half of the approximately 1000 matches I had. 

The training data was balanced so that there was an equal number of defender wins and attacker wins and only included matches that had at least 8 players. I had 3 types of evaluation data sets which were both unbalanced. The first evaluation data sets were matches with at least 2 players and at most 7 players and when the model was evaluated against this data set, it had about 57% accuracy. The second evaluation data set was matches within the in-game tournament system and had at least 7 or more players with a prediction accuracy of about 73%. The third data set was just against some of the training data with of course no duplicates as to avoid memorization, accuracy of this evaluation was about 63%, however since the training data set was so small and some of it was used for evaluation, the true accuracy is most likely higher. 

### Thoughts
Both models worked surprisingly well albeit with a bit of work needed to be put into it and considering it was my first time working with AI and a lot of other topics like data mining and parsing. The primary model that I worked on, the game outcome prediction model works better when it is missing less data (obviously), so when used in real world games, without the availability of prerecorded data, the model performs much worse, especially since in matches players in can hide their profiles exacerbating the already problematic missing data issue. 

Some future goals I have for the project are to try predicting matches in the pro scene since the dynamic and the way the game is played at the pro level is completely different than in casual games. I also want to try building on the outcome prediction and predicting the number of rounds each team will win.
