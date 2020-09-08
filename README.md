# Runanalyzer Java 

This bot is designed to integrate with [Strava](https://www.strava.com)

## GetUpdates 

the bot works through the method GetUpdates. 
Attention: does not work with webhooks 


## Application launch
To run, you need to set 8 args

args[0] - YourtelegramToken (yor can change - > [BotFather](https://t.me/BotFather))
Telegram token given without adding "bot" before the token key. 
For example: 5784352324:A4в56oAпАввАk4N35ве3е-DавDPп

args[1] - YourstravaClientSecret 

args[2] - YourstravaClientId
To start developing with the Strava API, you will need to make an application

1.If you have not already, go to [Strava reg](https://www.strava.com/register) and sign up for a Strava account. 

2.After you are logged in, go to [Strava settings](https://www.strava.com/settings/api) and create an app.

Configuration  HicariCP

args[3] - DBdriverName 

args[4] - jdbcUrl

args[5] - DBuserName

args[6] - password

args[7] - maximumPoolSize for HicariCP
