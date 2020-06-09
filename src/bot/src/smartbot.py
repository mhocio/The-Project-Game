import socket
import json
import string
import random
import uuid
from threading import get_ident
from enum import Enum
from threading import Thread
from time import sleep 
from random import randrange

BUFFER_SIZE = 5012

def bot_function(addr = '127.0.0.1'):
    print("I'm " + str(get_ident()))
    my_player = Player(_host=addr)
    my_player.start()

    while(True):
        my_player.leaveGoalArea()
        my_player.discoverAndTryToPickUpAll()
        if(my_player.is_carrying_piece):
            my_player.test()  # do not test if player is close to the base?
            if(my_player.is_carrying_piece):
                my_player.goAndPlacePiece()
    
    my_player.finish()
    my_player.close()

def randomString(stringLength = 8):
    letters = string.ascii_letters
    return ''.join(random.choice(letters) for i in range(stringLength))

class Role(Enum):
    LEADER: 1
    MEMBER: 2

class Team(Enum):
    RED: 3
    BLUE: 4

class Board:
    cells = []

    def __init__(self, x, y, h):
        self.cells = [[0 for col in range(x)] for row in range(y)]
        self.goal_area_height = h
        self.board_height = y
        self.board_width = x

    def set_cell(self, x, y, d):
        self.cells[y][x] = d
    
    def get_cell(self, x, y):
        return self.cells[y][x]

    def show(self):
        for r in self.cells:
            for c in r:
                print(c,end = " ")
            print()
        print('\n')


class Player:
    writing = True
    is_carrying_piece = False

    def __init__(self, _host = '127.0.0.1', _port = 8080):
        self.HOST = _host
        self.PORT = _port
        self.GUID = '0000'
        self.connect()
        
    def set_guid(self, guid):
        self.GUID = guid

    def reading_thread(self):
        while(True):
            rv = self.recv()
            rv = {k: v for k, v in rv.items() if v is not None}  # remove Nones from dict
            print("RECV: ", rv)
            if(rv['action'] == "end"):
                break
            # elif rv['action'] == 'start' and rv['status'] == "OK":
            #     print("BOT_READ start")
            #     self.writing = False
            #     pass
            # response for start message from host
            elif rv['action'] == 'discover' and rv['status'] == "OK":
                for field in rv['fields']:
                    self.board.set_cell(field['x'], field['y'], field['cell']['distance'])
            elif rv['action'] == 'test' and rv['status'] == "OK":
                # TODO test piece status update
                pass
            elif rv['action'] == 'move':
                if rv['status'] == "OK":
                    self.set_pos(rv['position']['x'], rv['position']['y'])
            elif rv['action'] == 'pickup' and rv['status'] == "OK":
                self.is_carrying_piece = True

            self.writing = True

    def set_host(self, host):
        self.host = host

    def get_host(self):
        return self.host

    def get_guid(self):
        return self.GUID

    def set_board(self, x, y, h):
        self.board = Board(x, y, h)
    
    def set_team(self, color):
        self.team = color

    def set_role(self, role):
        self.role = role

    def set_pos(self, pos_x, pos_y):
        self.pos_x = pos_x
        self.pos_y = pos_y

    def get_pos_x(self):
        return self.pos_x

    def get_pos_y(self):
        return self.pos_y

    def wait(self):
        while(self.writing == False):
            pass

    def place_piece(self):
        self.wait()
        TestMessage={
            "action": "test",
            "playerGuid": self.get_guid()
            }
        self.send(TestMessage)
        self.writing = False

    def move_right(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerGuid": self.get_guid(),
            "direction": "Right",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False

    def move_left(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerGuid": self.get_guid(),
            "direction": "Left",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False


    def move_down(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerGuid": self.get_guid(),
            "direction": "Down",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False
        
    def move_up(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerGuid": self.get_guid(),
            "direction": "Up",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False

    def move(self, x, y):

        x_direction = x - self.get_pos_x()
        y_direction = y - self.get_pos_y()
        horizontal_mode = True

        while abs(x_direction) > 0 or abs(y_direction) > 0:
            self.wait()
            x_direction = x - self.get_pos_x()
            y_direction = y - self.get_pos_y()

            # prev_pos = (self.get_pos_x(), self.get_pos_y())
            if horizontal_mode and abs(x_direction) == 0:
                horizontal_mode = not horizontal_mode
            if not horizontal_mode and abs(y_direction) == 0:
                horizontal_mode = not horizontal_mode

            if horizontal_mode and x_direction > 0:
                self.move_right()
            elif horizontal_mode and x_direction < 0:
                self.move_left()
            elif not horizontal_mode and y_direction > 0:
                self.move_down()
            elif not horizontal_mode and y_direction < 0:
                self.move_up()

            # after_pos = (self.get_pos_x(), self.get_pos_y())
            # TODO: checking if someone blocked, now going like zigzag
            horizontal_mode = not horizontal_mode

            self.board.show()

    def pickup(self):
        self.wait()
        MoveMessage={
            "action": "pickup",
            "playerGuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False
    
    def test(self):
        self.wait()
        MoveMessage={
            "action": "test",
            "playerGuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def place(self):
        self.wait()
        MoveMessage={
            "action": "place",
            "playerGuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def discover(self):
        self.wait()
        MoveMessage={
            "action": "discover",
            "playerGuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def finish(self):
        self.wait()
        MoveMessage={
            "action": "finish",
            "playerGuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def connect(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((self.HOST, self.PORT))
    
    def close(self):
        self.x.join()
        self.socket.close()
        print("CLOSING SOCKET")

    def send(self, message):
        print("\n@@@@@ sending @@@@@ : ", message)
        self.socket.sendall(bytes(json.JSONEncoder().encode(message), "utf-8"))

    def recv(self):
        message_string = self.socket.recv(BUFFER_SIZE)
        return json.loads(message_string)

    def start(self):
        message = {
            "action" : "connect",
            # "playerGuid" : randomString(),
            "playerGuid" : str(uuid.uuid1()),
        }
        
        print(f"connecting to {self.HOST} on {self.PORT}")

        self.send(message)
        print("sent connect message")

        connected = self.recv()
        print("CONNECTED")
        print(connected)
        
        config = {"action": "DENIED"}     
        # wait for start
        while config["action"] != "start":
            sleep(1)
            config = self.recv()
            print("startMessage while")
            print(config)
            
        if config["action"] == "start":
                self.set_board(config["board"]["boardWidth"], config["board"]["taskAreaHeight"] + 2 * config["board"]["goalAreaHeight"], config["board"]["goalAreaHeight"])
                self.set_pos(int(config["position"]["x"]), int(config["position"]["y"]))
                print(config["team"])
                self.set_team(config["team"])
                self.set_guid(config["playerGuid"])

                if config['status'] == 'OK':
                    self.x = Thread(target = self.reading_thread)
                    self.x.start()

    def discoverAndTryToPickUpAll(self):
        self.discover()
        px=self.get_pos_x()
        py=self.get_pos_y()
        self.board.show()
        # minVal = self.board.get_cell(self.get_pos_x(),self.get_pos_y())
        minVal = self.board.board_height * self.board.board_width
        for ix in range(self.get_pos_x()-1,self.get_pos_x()+1):
            for iy in range(self.get_pos_y()-1,self.get_pos_y()+1):
                if self.board.get_cell(ix,iy)<minVal and not (ix == self.get_pos_x() and iy == self.get_pos_y()):
                    px=ix
                    py=iy
                    minVal=self.board.get_cell(ix,iy)
        print("MINVAL:", minVal, " AT:", px, py)
        if((abs(self.get_pos_x()-px)+abs(self.get_pos_y()-py))<2):
            print("HV: ", self.get_pos_x()+(px-self.get_pos_x())*minVal,self.get_pos_y()+(py-self.get_pos_y())*minVal)
            self.move(self.get_pos_x()+(px-self.get_pos_x())*minVal,self.get_pos_y()+(py-self.get_pos_y())*minVal)
            self.pickup()
        else:
            for i in range(0,minVal):
                if(self.get_pos_x()+(px-self.get_pos_x())*i<0 or self.get_pos_x()+(px-self.get_pos_x())*i > self.board.board_width 
                or self.get_pos_y()+(py-self.get_pos_y())*(minVal-i)< 0 or self.get_pos_y()+(py-self.get_pos_y())*(minVal-i) > self.board.board_height):
                    continue
                print("D: ", self.get_pos_x()+(px-self.get_pos_x())*i, self.get_pos_y()+(py-self.get_pos_y())*(minVal-i))
                self.move(self.get_pos_x()+(px-self.get_pos_x())*i, self.get_pos_y()+(py-self.get_pos_y())*(minVal-i))
                self.pickup()
                if(self.is_carrying_piece):
                    break
        
    def leaveGoalArea(self):
        while(self.get_pos_y()<self.board.goal_area_height):
            self.move_down()  # may be blocked by player from the same team

        while(self.get_pos_y()>=self.board.board_height-self.board.goal_area_height):
            self.move_up()  # may be blocked by player from the same team

    def goAndPlacePiece(self):
        if self.team == "RED":
            self.move(randrange(self.board.board_width),self.board.board_height-self.board.goal_area_height+randrange(self.board.goal_area_height))
            self.place()
        else:
            self.move(randrange(self.board.board_width),randrange(self.board.goal_area_height))
            self.place()

