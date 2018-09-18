#Python Game
import turtle
import os
import time

#Set up the screen
wn = turtle.Screen()
wn.bgcolor("black")
wn.title("Space Invaders")

#Draw Border
pen = turtle.Turtle()
pen.speed(0)
pen.color("teal")
pen.penup()
pen.setposition(-300,-300)
pen.speed(2.8)
pen.pendown()
pen.pensize(3)

#loop that draws the border
for i in range(4):
    pen.fd(600)
    pen.lt(90)


pen.color("red")
pen.fd(600)
pen.up()
pen.fd(300)
pen.ht()


#Turn Pen int Player Turtle
pen.color("lime")
pen.shape("turtle")
pen.lt(90)
pen.speed(0)
pen.setposition(0,-299)
pen.speed(1)
time.sleep(.5)
pen.st()
pen.setposition(0,-225)


#pen2 for fun
pen2 = turtle.Turtle()
pen2.ht()
pen2.penup()
pen2.speed(0)
pen2.rt(90)
pen2.color("purple")
pen2.shape("turtle")

pen2.setposition(0,299)
pen2.st()
pen2.speed(1)
pen2.fd(480)#524
pen2.color("lime")
pen2.rt(360)
