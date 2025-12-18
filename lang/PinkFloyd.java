    package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.*;
    import mars.util.*;
    import mars.mips.instructions.*;
    import java.util.Random;
/**
 * To create a custom language, you must extend the CustomAssembly abstract class and override its three methods.
 * It must also be part of the mars.mips.instructions.customlangs package.
 * 
 * The populate() method is where the magic happens - you must specify your instructions to be added here.
 * For more examples regarding the instruction format, you can view the implementation of the MIPS instructions in mars/mips/instructions/MipsAssembly.java.
 * 
 * Instructions to get your custom language into MARS:
 * Navigate to the MARS folder in your command terminal and build a JAR file from your custom assembly file.
 * Ensure that the internal folder structure is correct, or the JAR will be broken and MARS won't recognize it.
 * To do this on Windows, input the following commands after navigating to .../MARS/:
 * 
 * javac -d out mars/mips/instructions/customlangs/{NAME OF YOUR LANGUAGE}.java
 * jar cf {NAME OF YOUR LANGUAGE}.jar -C out .
 * rmdir /S /Q out
 * 
 * This will leave you with a working JAR file in the MARS directory containing your custom language. 
 * Drop it into the customlangs folder and it will appear under the Language Switcher tool.
 * @see CustomAssembly
 */
public class PinkFloyd extends CustomAssembly{
    @Override
    public String getName(){
        return "Pink Floyd";
    }

    @Override
    public String getDescription(){
        return "Make a recipe for your own Pink Floyd song using memes and references to the music!";
    }

    @Override
    protected void populate(){
        instructionList.add(        // add
                new BasicInstruction("rick $t1,$t2,$t3",
            	 "Addition with overflow : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 000001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = RegisterFile.getValue(operands[2]);
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));
         instructionList.add(       // addi
                new BasicInstruction("nick $t1,$t2,-100",
            	 "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT,
                "000001 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = operands[2] << 16 >> 16;
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));
        instructionList.add(        // jump
                new BasicInstruction("fly target", 
            	 "Jump unconditionally : Jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));
        instructionList.add(        // jal
                new BasicInstruction("ltf target", 
            	 "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000011 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister("$v0", RegisterFile.getProgramCounter());
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));
        instructionList.add(        // sw
                new BasicInstruction("stay $t1,-100($t2)", 
            	 "Store word : Store contents of $t1 into effective memory word address",
            	 BasicInstructionFormat.I_FORMAT,
                "000101 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try {
                        Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1], RegisterFile.getValue(operands[0]));
                     } catch (AddressErrorException e) {
                        throw new ProcessingException(statement, e);
                     }
                  }
               }));
        instructionList.add(        // lw
                new BasicInstruction("wot $t1,-100($t2)", 
            	 "Load word : Set $t1 to contents of effective memory word address offset($t2)",
            	 BasicInstructionFormat.I_FORMAT,
                "001001 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try {
                        RegisterFile.updateRegister(operands[0], Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]));
                     } catch (AddressErrorException e) {
                        throw new ProcessingException(statement, e);
                     }
                  }
               }));
        instructionList.add(        // move
                new BasicInstruction("run $t1, $t2", 
            	 "Move : Set $t1 to the contents of $t2",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss fffff 00000 00000 100001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                  }
               }));
        instructionList.add(        // syscall
                new BasicInstruction("echoes", 
            	 "Issue a system call : Execute the system call specified by value in $v0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 111111",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Globals.instructionSet.findAndSimulateSyscall(RegisterFile.getValue(2), statement);
                  }
               }));
        instructionList.add(        // sub
                new BasicInstruction("diff $t1, $t2, $t3", 
            	 "Subtraction with overflow : set $t1 to ($t2 minus $t3)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss fffff ttttt 00000 000010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int sub1 = RegisterFile.getValue(operands[1]);
                     int sub2 = RegisterFile.getValue(operands[2]);
                     int out = sub1 - sub2;
                     if ((sub1 < 0 || sub2 >= 0 || out >= 0) && (sub1 >= 0 || sub2 < 0 || out < 0)) {
                        RegisterFile.updateRegister(operands[0], out);
                     } else {
                        throw new ProcessingException(statement, "arithmetic overflow", 12);
                     }
                  }
               }));
        instructionList.add(        // sb
                new BasicInstruction("sit $t1,-100($t2)", 
            	 "Store byte : Store the low-order 8 bits of $t1 into the effective memory byte address",
            	 BasicInstructionFormat.I_FORMAT,
                "000111 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try {
                       Globals.memory.setByte(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16), RegisterFile.getValue(operands[0]) & 255);
                     } catch (AddressErrorException e) {
                       throw new ProcessingException(statement, e);
                     }
                  }
               }));
        instructionList.add(        // syd
                new BasicInstruction("syd", 
            	 "Syd Barrett : Print random lyric from syd or lyric referencing syd",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random r = new Random();
                     int s0 = RegisterFile.getValue(16);
                     int s1 = RegisterFile.getValue(17);
                     int s5 = RegisterFile.getValue(21);

                     if (s1 < 2 && s5 == 0) {
                        s0++;
                        s1++;
                        RegisterFile.updateRegister(16, s0);
                        RegisterFile.updateRegister(17, s1);

                        switch(r.nextInt(6)) {
                            case 0:
                                SystemIO.printString("I've got a bike, you can ride it if you like :)\n");
                                SystemIO.printString("It's got a basket a bell that rings, and things to make it look good :)\n");
                                SystemIO.printString("I'd give it to you if I could, but I borrowed it :)\n");
                                break;
                            case 1:
                                SystemIO.printString("You're the kind of girl that fits in with my world\n");
                                SystemIO.printString("I'll give you everything, anything if you want things\n");
                                break;
                            case 2:
                                SystemIO.printString("I know a mouse and he hasn't got a house\n");
                                SystemIO.printString("I don't know why I call him Gerald\n");
                                SystemIO.printString("He's getting rather old, but he's a good mouse\n");
                                break;
                            case 3:
                                SystemIO.printString("I really love you and I mean you\n");
                                SystemIO.printString("The star above you, crystal blue\n");
                                SystemIO.printString("I wouldn't see you, and I love to\n");
                                SystemIO.printString("I fly above you, yes I do\n");
                                break;
                            case 4:
                                SystemIO.printString("Lime and limpid green, a second scene, a fight between the blue you once knew\n");
                                SystemIO.printString("Floating down, the sound resounds around the icy waters underground\n");
                                break;
                            case 5:
                                SystemIO.printString("I want to tell you a story, about a little man, if i can\n");
                                SystemIO.printString("A gnome named Grimble Crumble; And little gnomes stay in their homes\n");
                                SystemIO.printString("He had a big adventure, amidst the grass, fresh air at last\n");
                                SystemIO.printString("And then one day, Hooray!\n");
                                break;
                        }
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     } else if (s5 == 0) {
                        s0++;
                        s1++;
                        RegisterFile.updateRegister(16, s0);
                        RegisterFile.updateRegister(17, s1);

                        switch(r.nextInt(10)) {
                            case 0:
                                SystemIO.printString("Remember when you were young?\n");
                                SystemIO.printString("You shone like the sun\n");
                                SystemIO.printString("Now there's a look in your eyes\n");
                                SystemIO.printString("Like black holes in the sky\n");
                                break;
                            case 1:
                                SystemIO.printString("You were caught in the crossfire, Of childhood and stardom\n");
                                SystemIO.printString("Blown on the steel breeze\n");
                                SystemIO.printString("Come on you target, for faraway laughter\n");
                                SystemIO.printString("Come on you stranger, you legend, you martyr, and Shine!\n");
                                break;
                            case 2:
                                SystemIO.printString("You reached for the secret too soon\n");
                                SystemIO.printString("You cried for the moon\n");
                                SystemIO.printString("Threatend by shadows at night\n");
                                SystemIO.printString("And exposed in the light\n");
                                break;
                            case 3:
                                SystemIO.printString("Well, you wore out your welcome, with random precision\n");
                                SystemIO.printString("Rode on the steel breeze\n");
                                SystemIO.printString("Come on you raver, you seer of visions,\n");
                                SystemIO.printString("Come on you painter, you piper, you prisoner, and Shine!\n");
                                break;
                            case 4:
                                SystemIO.printString("Nobody knows where you are, how near or how far\n");
                                break;
                            case 5:
                                SystemIO.printString("Pile on many more layers, and I'll be joining you there\n");
                                break;
                            case 6:
                                SystemIO.printString("And we'll bask in the shadow of yesterday's triumph\n");
                                SystemIO.printString("Sail on the steel breeze\n");
                                SystemIO.printString("Come on you boy child, you winner and loser\n");
                                SystemIO.printString("Come on you miner for truth and delusion, and Shine!\n");
                                break;
                            case 7:
                                SystemIO.printString("So, so you think you can tell\n");
                                SystemIO.printString("Heaven from Hell?\n");
                                SystemIO.printString("Blue skies from pain?\n");
                                SystemIO.printString("Can you tell a green field,\n");
                                SystemIO.printString("from a cold steel rail?\n");
                                SystemIO.printString("A smile from a veil?\n");
                                SystemIO.printString("Do you think you could tell?\n");
                                break;
                            case 8:
                                SystemIO.printString("Did the get you to trade?\n");
                                SystemIO.printString("Your heroes for ghosts?\n");
                                SystemIO.printString("Hot ashes for trees?\n");
                                SystemIO.printString("Hot air for a cool breeze?\n");
                                SystemIO.printString("Cold comfort for change?\n");
                                SystemIO.printString("Did you exchange?\n");
                                SystemIO.printString("A walk on part in the war,\n");
                                SystemIO.printString("For a lead role in a cage?\n");
                                break;
                            case 9:
                                SystemIO.printString("We're just two lost souls swimming in a fishbowl,\n");
                                SystemIO.printString("year after year,\n");
                                SystemIO.printString("running over the same old ground,\n");
                                SystemIO.printString("what have we found?\n");
                                SystemIO.printString("The same old fears,\n");
                                SystemIO.printString("Wish you were here.\n");
                                break;
                        }
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // rog
                new BasicInstruction("rog", 
            	 "Roger Waters : Print a random lyric from rog",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random r = new Random();
                     int s0 = RegisterFile.getValue(16);
                     int s2 = RegisterFile.getValue(18);
                     int s3 = RegisterFile.getValue(19);
                     int gilmie_lead = s3 - s2;

                     if (gilmie_lead < 5) {
                        s0++;
                        s2++;
                        RegisterFile.updateRegister(16, s0);
                        RegisterFile.updateRegister(18, s2);

                        switch(r.nextInt(12)) {
                            case 0:
                                SystemIO.printString("Daddy's flown across the ocean\n");
                                SystemIO.printString("Leaving just a memory\n");
                                SystemIO.printString("\n");
                                break;
                            case 1:
                                SystemIO.printString("A snapshot in the family album\n");
                                SystemIO.printString("Daddy, what else did you leave for me?\n");
                                SystemIO.printString("\n");
                                break;
                            case 2:
                                SystemIO.printString("Daddy what'd ya leave behind for me?\n");
                                SystemIO.printString("\n");
                                break;
                            case 3:
                                SystemIO.printString("Overhead the albatross\n");
                                SystemIO.printString("Hangs motionless upon the air\n");
                                SystemIO.printString("And deep beneath the rolling waves\n");
                                SystemIO.printString("In labyrinths of coral caves\n");
                                SystemIO.printString("The echo of a distant time\n");
                                SystemIO.printString("Comes willowing across the sand\n");
                                SystemIO.printString("And everything is green and submarine\n");
                                break;
                            case 4:
                                SystemIO.printString("Strangers passing in the street\n");
                                SystemIO.printString("By chance, two separate glances meet\n");
                                SystemIO.printString("And I am you and what I see is me\n");
                                SystemIO.printString("And do I take you by the hand,\n");
                                SystemIO.printString("And lead you through the land,\n");
                                SystemIO.printString("And help me understand the best I can?\n");
                                break;
                            case 5:
                                SystemIO.printString("Tired of lying in the sunshine\n");
                                SystemIO.printString("Staying home to watch the rain\n");
                                SystemIO.printString("You are young and life is long\n");
                                SystemIO.printString("And there is time to kill today\n");
                                SystemIO.printString("But then one day you find\n");
                                SystemIO.printString("Ten years have got behind you\n");
                                SystemIO.printString("No one told you when to run\n");
                                SystemIO.printString("You missed the starting gun\n");
                                break;
                            case 6:
                                SystemIO.printString("You gotta be crazy, you gotta have a real need\n");
                                SystemIO.printString("Gotta sleep on your toes, and when you're on the street\n");
                                SystemIO.printString("Got to be able to pick out the easy meat with your eyes closed\n");
                                SystemIO.printString("Then moving in silently, downwind and out of sight\n");
                                SystemIO.printString("You got to strike when the moment is right without thinking\n");
                                SystemIO.printString("And after a while, you can work on points for style\n");
                                SystemIO.printString("Like the club tie, and the firm handshake\n");
                                SystemIO.printString("A certain look in the sky and an easy smile\n");
                                SystemIO.printString("You have to be trusted by the people that you lie to\n");
                                SystemIO.printString("So that when they turn their backs on you\n");
                                SystemIO.printString("You get the chance to put the knife in\n");
                                break;
                            case 7:
                                SystemIO.printString("And when you lose control\n");
                                SystemIO.printString("You'll reap the harvet you have sown\n");
                                SystemIO.printString("And as the fear grows\n");
                                SystemIO.printString("The bad blood slows and turns to stone\n");
                                SystemIO.printString("And it's too late to lose the weight\n");
                                SystemIO.printString("You used to need to throw around\n");
                                SystemIO.printString("So have a good drown, as you go down, all alone\n");
                                SystemIO.printString("Dragged down by the stone\n");
                                break;
                            case 8:
                                SystemIO.printString("For long you life and high you fly\n");
                                SystemIO.printString("And smile you'll give and tears you'll cry\n");
                                SystemIO.printString("And all you touch and all you see\n");
                                SystemIO.printString("Is all your life will ever be\n");
                                SystemIO.printString("\n");
                                SystemIO.printString("Run, rabbit, run\n");
                                SystemIO.printString("Dig that hole, forget the sun\n");
                                SystemIO.printString("And when at last the work is done\n");
                                SystemIO.printString("Don't sit down it's time to dig another one\n");
                                SystemIO.printString("\n");
                                SystemIO.printString("For long you life and high you fly\n");
                                SystemIO.printString("But only if you ride the tide\n");
                                SystemIO.printString("Balanced on the biggest wave\n");
                                SystemIO.printString("You race towards an early grave\n");
                                break;
                            case 9:
                                SystemIO.printString("Hey, you\n");
                                SystemIO.printString("Out there on the road, always doing what you're told, can you help me?\n");
                                SystemIO.printString("Hey, you\n");
                                SystemIO.printString("Out there beyond the wall, breaking bottles in the hall, can you help me?\n");
                                SystemIO.printString("Hey, you, don't tell me there's no hope at all\n");
                                SystemIO.printString("Together we stand, divided we fall\n");
                                SystemIO.printString("Hey, you, would you help me to carry the stone?\n");
                                SystemIO.printString("Open your heart, I'm coming home\n");
                                break;
                            case 10:
                                SystemIO.printString("Through the fisheyed lens of tear stained eyes\n");
                                SystemIO.printString("I can barely make out the shape of this moment in time\n");
                                break;
                            case 11:
                                SystemIO.printString("Harmlessly passing your time in the grassland away\n");
                                SystemIO.printString("Only dimly aware of a certain unease in the air\n");
                                SystemIO.printString("You better watch out! There may be dogs about!\n");
                                SystemIO.printString("I've looked over Jordan and I've seen things are not what the seem\n");
                                SystemIO.printString("\n");
                                SystemIO.printString("What do you get for pretending the danger's not real?\n");
                                SystemIO.printString("Meek and obedient you follow the leader\n");
                                SystemIO.printString("Down well trodden corridors into the valley of steel\n");
                                SystemIO.printString("\n");
                                SystemIO.printString("What a suprise!\n");
                                SystemIO.printString("A look of terminal shock in your eyes!\n");
                                SystemIO.printString("Now things are really what they seem!\n");
                                SystemIO.printString("No! This is no bad dream!\n");
                                break;
                        }
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // gilmie
                new BasicInstruction("gilmie", 
            	 "David Gilmour : Print a rand length guitar solo",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101011",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random r = new Random();
                     int s0 = RegisterFile.getValue(16);
                     int s2 = RegisterFile.getValue(18);
                     int s3 = RegisterFile.getValue(19);
                     int s5 = RegisterFile.getValue(21);
                     int rog_lead = s2 - s3;

                     if (rog_lead < 5 && s5 == 0) {
                        s0++;
                        s3++;
                        RegisterFile.updateRegister(16, s0);
                        RegisterFile.updateRegister(19, s3);

                        int minute_max = r.nextInt(13) + 7;
                        int m_rand = r.nextInt(minute_max);
                        int s_rand = r.nextInt(59);

                        SystemIO.printString("{" + m_rand + ":" + s_rand + "} GUITAR SOLO!!!\n");
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // money
                new BasicInstruction("money", 
            	 "Money : Print sick bass line",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);

                     if (s5 == 0) {
                        s0++;
                        RegisterFile.updateRegister(16, s0);

                        SystemIO.printString("*sick bass line intensifies*\n");
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // quit
                new BasicInstruction("quit", 
            	 "Quit : Try to leave the music industry`",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101101",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     s0++;
                     RegisterFile.updateRegister(16, s0);

                     SystemIO.printString("THE SHOW MUST GO ON!!!\n");
                     SystemIO.printString("\n\n\n");

                     int s4 = RegisterFile.getValue(20);
                     int stone = 0;
                     if (s4 != 0) {stone = s0 - s4;}
                     if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                  }
               }));
        instructionList.add(        // parry
                new BasicInstruction("parry", 
            	 "Dick Parry : Print soulful sax solo",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101110",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);

                     if (s5 == 0) {
                        s0++;
                        RegisterFile.updateRegister(16, s0);

                        SystemIO.printString("*soulful sax solo subsumes*\n");
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // dogs
                new BasicInstruction("dogs", 
            	 "Dogs : Print dog sounds",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101111",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random r = new Random();
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);

                     if (s5 == 0) {
                        s0++;
                        RegisterFile.updateRegister(16, s0);

                        SystemIO.printString("bark bark bark BAAARK\n");
                        SystemIO.printString("\n");

                        if (r.nextInt(11) == 1) {
                            SystemIO.printString("I was in the kitchen\n");
                            SystemIO.printString("Seamus, that's the dog was outside\n");
                            SystemIO.printString("\n");
                            SystemIO.printString("bark bark bark BAAARK\n");
                            SystemIO.printString("\n");
                        }

                        SystemIO.printString("\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // pigs
                new BasicInstruction("pigs", 
            	 "Pigs : Speak to the pigs",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 110000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);

                     if (s5 == 0) {
                        s0++;
                        RegisterFile.updateRegister(16, s0);

                        SystemIO.printString("You radiate cold shafts of broken glass!\n");
                        SystemIO.printString("You're nearly a laugh, but you're really a cry\n");
                        SystemIO.printString("\n");
                        SystemIO.printString("HAHA!  CHARADE YOU ARE!\n");
                        SystemIO.printString("\n");
                        SystemIO.printString("You're trying to keep our feelings off the street!\n");
                        SystemIO.printString("You're nearly a treat, but you're really a cry\n");
                        SystemIO.printString("\n\n\n");

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // wall
                new BasicInstruction("wall", 
            	 "Wall : Rog builds his wall, no one else can be heard until the trial",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 110110",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);
                    
                     switch(s5) {
                        case 0:
                            SystemIO.printString("What shall we sue to fill the empty spaces\n");
                            SystemIO.printString("Where we used to talk?\n");
                            SystemIO.printString("How shall I fill the final places?\n");
                            SystemIO.printString("How shall I complete the wall?\n");
                            break;
                        case 1:
                            SystemIO.printString("Mother did it need to be so high?\n");
                            break;
                     }
                     s0++;
                     s5 = 1;
                     RegisterFile.updateRegister(16, s0);
                     RegisterFile.updateRegister(21, s5);

                     SystemIO.printString("\n\n\n");

                     int s4 = RegisterFile.getValue(20);
                     int stone = 0;
                     if (s4 != 0) {stone = s0 - s4;}
                     if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                  }
               }));
         instructionList.add(        // trial
                new BasicInstruction("trial", 
            	 "Trial : Put rog on trial to tear down his wall",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 110111",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     int s5 = RegisterFile.getValue(21);

                     if (s5 == 1) {
                        s0++;
                        RegisterFile.updateRegister(16, s0);

                        SystemIO.printString("Good morning, Worm, your honour\n");
                        SystemIO.printString("The crown will plainly show the prisoner\n");
                        SystemIO.printString("Who now stands before you\n");
                        SystemIO.printString("Was caught red-handed, showing feelings\n");
                        SystemIO.printString("Showing feelings of an almost human nature\n");
                        SystemIO.printString("This will not do\n");
                        SystemIO.printString("\n");
                        SystemIO.printString("Crazy, toys in the attic, I am crazy\n");
                        SystemIO.printString("Truly gone fishing\n");
                        SystemIO.printString("They must have taken my marbles away\n");
                        SystemIO.printString("Crazy, over the rainbow I am crazy\n");
                        SystemIO.printString("Bars in the window\n");
                        SystemIO.printString("There must have been a door there in the wall\n");
                        SystemIO.printString("When I came in (crazy, over the rainbow he is crazy)\n");
                        SystemIO.printString("\n");
                        SystemIO.printString("The evidence before the court is incontrovertible\n");
                        SystemIO.printString("There's no need for the jury to retire\n");
                        SystemIO.printString("In all my years of judging, I have never heard before\n");
                        SystemIO.printString("Of someone more deserving of the full penalty of the law\n");
                        SystemIO.printString("The way you made them suffer, your exquisite wife and mother,\n");
                        SystemIO.printString("Fills me with the urge to defecate\n");
                        SystemIO.printString("(go on Judge, shit on him!)\n");
                        SystemIO.printString("Since, my friend, you have revealed your deepest fear\n");
                        SystemIO.printString("I sentence you to be exposed before your peers\n");
                        for (int i = 1; i <= 10; i++) {
                            SystemIO.printString("Tear down the wall!\n");
                        }
                        SystemIO.printString("\n\n\n");

                        s5 = 0;
                        RegisterFile.updateRegister(21, s5);

                        int s4 = RegisterFile.getValue(20);
                        int stone = 0;
                        if (s4 != 0) {stone = s0 - s4;}
                        if (stone == 10) {SystemIO.printString("stone stone stone stone stone stone stone stone stone stone\nstone stone stone stone stone stone stone stone stone stone\n\n\n");}
                     }
                  }
               }));
        instructionList.add(        // stone
                new BasicInstruction("stone", 
            	 "Stone : Dragged down by the stone (and then again after 10 more songbite calls)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 101000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int s0 = RegisterFile.getValue(16);
                     s0++;
                     RegisterFile.updateRegister(16, s0);
                     RegisterFile.updateRegister(20, s0);   // set $s4 to $s0 aka curr songbite count

                     SystemIO.printString("Who was born in a house full of pain?\n");
                     SystemIO.printString("Who was trained not to spit in the fan?\n");
                     SystemIO.printString("Who was told what to do by the man?\n");
                     SystemIO.printString("Who was broken by trained personnel?\n");
                     SystemIO.printString("Who was fitted with collar and chain?\n");
                     SystemIO.printString("Who was given a pat on the back?\n");
                     SystemIO.printString("Who was breaking away from the pack?\n");
                     SystemIO.printString("Who was only a stranger at home?\n");
                     SystemIO.printString("Who was ground down in the end?\n");
                     SystemIO.printString("Who was found dead on the phone?\n");
                     SystemIO.printString("Who was dragged down by the stone?\n");
                     SystemIO.printString("WHO WAS DRAGGED DOWN BY THE STONE?\n");
                     SystemIO.printString("STONE STONE STONE STONE STONE STONE STONE STONE STONE STONE\n");
                     SystemIO.printString("STONE STONE STONE STONE STONE STONE STONE STONE STONE STONE\n");
                     SystemIO.printString("\n\n\n");
                     
                  }
               }));
    }
}