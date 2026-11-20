ANG HULING TANOD

"May natitira pa tayo."

2D Tower Defense and Survival Game

Final Project Documentation
Object-Oriented Programming (OOP)
Bachelor of Science in Information Technology – BSIT 3A
University of Batangas – Lipa City

Final Submission: November 30, 2026
Live Demonstration: December 1, 2026

1. Project Overview

Ang Huling Tanod is a 2D tower defense and survival game developed using Java Swing and AWT without relying on external game engines or libraries.

The game follows the story of the last tanod (village guard) of Isla Dulo, the final free island during a fictional invasion. The player's main objective is to protect the island and survive seven nights against invading enemy ships.

During the daytime, players collect resources, harvest fish, mine ores, and build defensive structures. During nighttime, players operate a lighthouse to illuminate the surrounding sea, locate approaching enemy ships, and defend the island.

The lighthouse is an important gameplay mechanic because players can only see enemies and objects illuminated by its beam.

The game combines resource management, strategic defense, survival mechanics, and Filipino-inspired storytelling.

2. Project Objectives

The main objectives of Ang Huling Tanod are:

To develop a playable 2D survival and tower defense game using Java.

To apply Object-Oriented Programming concepts in an actual software project.

To demonstrate the use of Java Swing and AWT for game development.

To implement resource gathering, inventory management, and defensive structures.

To create an interactive day-and-night game cycle.

To develop an engaging survival experience through enemies, challenges, and strategic gameplay.

To incorporate Filipino-inspired storytelling and cultural elements.

3. Game Story and Concept

The story takes place on Isla Dulo, a fictional island that remains free after an invading empire known as the Kordan Dominion begins taking control of surrounding territories.

The player takes the role of the island's last tanod. With limited resources and defenses, the player must protect the lighthouse, maintain the radio signal, and prevent enemy forces from invading the island.

The lighthouse serves as the island's primary defense and source of visibility. The player must carefully control its beam to detect enemy ships in the darkness.

The radio is equally important because it allows the island to send a signal to the outside world, proving that someone is still alive.

The player's mission is simple:

Survive seven nights and keep the island alive.

4. Game Features

The game includes the following features:

4.1 Day and Night Cycle

Daytime is used for harvesting, mining, and building defenses.

Nighttime is used for defending the island against enemy ships.

Players must prepare before nighttime begins.

4.2 Resource Gathering

Players can collect different resources to maintain and upgrade their defenses.

Available resources include:

Bamboo

Nickel

Copper

Chromite

Gold

Fish

4.3 Lighthouse System

Players control the lighthouse beam using the mouse.

The beam illuminates enemies and objects.

Only illuminated areas are visible during nighttime.

Players can upgrade the lighthouse to increase beam width and distance.

4.4 Defensive Structures

Players can construct defensive equipment to protect the island.

Nets – Used to defend shallow-water areas.

Mines – Used as another defensive mechanism against invading ships.

Post Armor – Increases the maximum health of the defensive post.

4.5 Enemy Ships

The game features different types of invading ships:

Skiff – The first enemy ship.

Cutter – A stronger enemy vessel.

Barge – Another type of invading ship.

4.6 Radio System

The radio allows players to maintain communication with the outside world.

Players can repair and rewire the radio using collected resources to improve the island's signal.

4.7 Survival System

Players must protect the island's post and radio while surviving seven nights.

The game includes winning and losing conditions.

5. Game Controls

Key / Input

Function

Enter

Start the game

Mouse Movement

Aim the lighthouse beam

P or Esc

Pause or resume

M

Return to the main menu while paused

Hold Left Click (Day)

Harvest resources

1

Activate or deactivate the net tool

2

Activate or deactivate the mine tool

Right Click

Put away the selected tool

R

Repair the post and radio

C

Rewire the radio

V

Upgrade post armor

G

Upgrade the lighthouse lamp

Left Click (Night)

Fire at illuminated enemy ships

N

Skip to the next phase (testing only)

6. Resource Management

The player must collect and manage resources to survive.

Resource

Purpose

Bamboo

Used to construct nets

Nickel

Used to construct mines

Copper

Used to rewire the radio

Chromite

Used to upgrade post armor

Gold

Used to upgrade the lighthouse

Fish

Used to repair the post and radio

Resource gathering is limited, so players must carefully decide when and how to use their collected materials.

7. Development Technologies

The project uses the following technologies:

Programming Language: Java

Development Environment: Apache NetBeans

Java Development Kit: JDK 26

Libraries and Frameworks:

Java Swing – Used to create the game window and graphical interface.

Java AWT – Used for graphics rendering, mouse input, and keyboard controls.

Data Storage:

Text files for island maps, enemy waves, and resource locations.

Planned high-score file for saving player records.

Game Engine: Custom-built Java game loop (no external game engine).

8. Project Development Status

The project is currently a work in progress. The playable core has been implemented, while additional features are still planned.

Feature

Status

Resizable game window and game loop

Completed

Main menu, play, and pause screens

Completed

Island tile map

Completed

Lighthouse beam and nighttime visibility

Completed

First enemy ship (Skiff)

Completed

Day and night cycle

Completed

Resource harvesting

Completed

Nets, post, and radio

Completed

HUD bars and survival conditions

Completed

Wave and resource data files

Completed

Cutter and Barge enemy ships

Completed

Mine buoys

Completed

Four types of ores and five types of fish

Completed

Sprite graphics

In Progress

Sound effects

Planned

High-score system

Planned

Infiltrators and special characters

Planned

Babaylan and Bakunawa

Planned

Boss battles

Planned

9. Installation and How to Run

Requirements

Apache NetBeans

JDK 26

Ang Huling Tanod project repository

Running Through NetBeans

Clone or download the project repository.

Open Apache NetBeans.

Select File → Open Project.

Locate and select the AngHulingTanod folder.

Open the project.

Right-click the project and select Run.

Alternatively, press F6 to execute the program.

Main Class: atingdagat.core.Main

Running Through JAR File

Once the final JAR file is available, execute:

java -jar AngHulingTanod.jar

10. Project Structure

The project follows an organized package structure to separate different game components.

Ang-Huling-Tanod/
│
├── README.md
├── CREDITS.md
│
├── docs/
│   ├── 01-game-design.md
│   ├── 02-project-plan.md
│   ├── 03-code-guide.md
│   ├── 04-pitch-and-demo.md
│   ├── 05-git-workflow.md
│   ├── 06-playtest-log.md
│   ├── 07-art-prompts.md
│   ├── 08-file-reference.md
│   ├── 09-build-story.md
│   └── 10-complete-guide.md
│
└── AngHulingTanod/
    └── src/
        └── atingdagat/
            ├── core/
            ├── map/
            ├── entities/
            ├── defense/
            ├── economy/
            ├── ui/
            ├── util/
            └── data/

Package Descriptions

core – Contains the main class, game loop, game states, and day-night cycle.

map – Handles the island and surrounding sea tile map.

entities – Contains the lighthouse, enemy ships, post, and radio.

defense – Contains defensive structures such as nets and mines.

economy – Manages resources, fish, harvesting, and inventory.

ui – Handles the HUD, nighttime overlay, popups, and explosions.

util – Contains asset-loading utilities.

data – Stores island maps, enemy waves, and resource locations.

11. Team Members and Contributions

Member

Assigned Role

Gabriel Gabiliño

Lead Programmer – Architecture, game logic, game loop, file handling, and sound

Gio Alvez

Presentation slides, game demonstration, playtesting, and one-page project proposal

Jaron

Art assets, README documentation, UML diagram, and credits

Note: Specific contributions should be reviewed and updated before final submission.

12. Originality and Creativity

Ang Huling Tanod is an original group project developed for the Object-Oriented Programming course.

Its story, gameplay concept, and mechanics were created by the group and approved by the professor.

The Kordan Dominion is a fictional invading empire and does not represent any real country or government.

The project also incorporates Filipino-inspired folklore and storytelling elements to give the game a unique identity.

13. Future Improvements

The development team plans to improve the game by adding:

Complete character and enemy sprite animations.

Background music and sound effects.

A high-score saving system.

Additional enemy types and infiltrators.

Special characters inspired by Filipino folklore.

Bakunawa and other boss battles.

More defensive structures and upgrades.

Improved graphics and user interface.

Additional playtesting and gameplay balancing.

14. Credits and Acknowledgment

The development of Ang Huling Tanod is a collaborative effort of the BSIT 3A students of the University of Batangas – Lipa City.

The team acknowledges the contributions of its members in programming, graphic design, documentation, testing, and project presentation.

For third-party assets, licensing information, and AI-assisted art disclosure, please refer to the separate CREDITS.md file.

15. Conclusion

Ang Huling Tanod demonstrates how Java programming and Object-Oriented Programming concepts can be applied to create an interactive 2D survival and tower defense game.

Through resource management, strategic defense, enemy encounters, and a day-and-night system, players experience the challenge of protecting the last free island.

More than a survival game, Ang Huling Tanod represents courage, teamwork, and hope in the face of adversity.

"May natitira pa tayo."

Final Project – Object-Oriented Programming
BSIT 2B | University of Batangas – Lipa City
November 30, 2026
