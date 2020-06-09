import socket
import json
import string
import random
import uuid
from threading import get_ident
from enum import Enum
from threading import Thread
from time import sleep 

BUFFER_SIZE = 5000

def bot_function(addr):
    print("I'm " + str(get_ident()))
    my_player = Player(_host=addr)
    
    my_player.start()
    my_player.move_right()
    my_player.move_left()
    my_player.move_down()
    my_player.move_up()
    my_player.finish()
    my_player.close()
    print("END BOT FUNCTION")

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

    def set_cell(self, x, y, d):
        self.cells[x][y] = d
    
    def get_cell(self, x, y):
        return self.cells[x][y]


class Player:
    writing = True
    is_carrying_piece = False

    def __init__(self, _host = '127.0.0.1', _port = 1300):
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
                print("BOT_READ finish")
                break
            # elif rv['action'] == 'start' and rv['status'] == "OK":
            #     print("BOT_READ start")
            #     self.writing = False
            #     pass
            # response for start message from host
            elif rv['action'] == 'discover' and rv['status'] == "OK":
                print("BOT_READ discover")
                for field in rv['fields']:
                    self.board.set_cell(field['x'], field['y'], field['cell']['distance'])
            elif rv['action'] == 'test' and rv['status'] == "OK":
                # TODO test piece status update
                pass
            elif rv['action'] == 'move':
                print("BOT_READ position before: "+str(self.get_pos_x())+" "+str(self.get_pos_y()))
                if rv['status'] == "OK":
                    self.set_pos(rv['position']['x'], rv['position']['y'])
                print("BOT_READ move")
                print("BOT_READ position after: "+str(self.get_pos_x())+" "+str(self.get_pos_y()))
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
        print("WRITING "+str(self.writing))

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
        print("WRITING before "+str(self.writing))
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
        print("WRITING after "+str(self.writing))

    def move_left(self):
        self.wait()
        print("POSITION before "+str(self.get_pos_x())+" "+str(self.get_pos_y()))
        print("WRITING before "+str(self.writing))
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
        print("POSITION after "+str(self.get_pos_x())+" "+str(self.get_pos_y()))
        print("WRITING after "+str(self.writing))


    def move_up(self):
        self.wait()
        print("WRITING before "+str(self.writing))
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
        print("WRITING after "+str(self.writing))
        
    def move_down(self):
        self.wait()
        print("WRITING before "+str(self.writing))
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
        print("WRITING after "+str(self.writing))

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
                self.move_up()
            elif not horizontal_mode and y_direction < 0:
                self.move_down()

            # after_pos = (self.get_pos_x(), self.get_pos_y())
            # TODO: checking if someone blocked, now going like zigzag
            horizontal_mode = not horizontal_mode

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
        self.socket.sendall(bytes(json.JSONEncoder().encode(message) + '\n', "utf-8"))
        self.socket.sendmsg

    def recv(self):
        # message_string = self.socket.recv(BUFFER_SIZE)
        return json.loads(self.socket.recv(BUFFER_SIZE))

    def start(self):
        message = {
            "action" : "connect",
            "playerGuid" : str(uuid.uuid1()),
        }

        self.send(message)
        print("sent")

        connected = self.recv()
        print("CONNECTED")
        print(connected)
        
        config = {"action": "DUPA"}     
        # wait for start
        
        while config["action"] != "start":
            sleep(1)
            config = self.recv()
            print("startMessage while")
            print(config)
            
        if config["action"] == "start":
                self.set_board(config["board"]["boardWidth"], config["board"]["taskAreaHeight"] + config["board"]["goalAreaHeight"], config["board"]["goalAreaHeight"])
                self.set_pos(int(config["position"]["x"]), int(config["position"]["y"]))
                print(config["team"])
                self.set_team(config["team"])
                self.set_guid(config["playerGuid"])

                # if config['status'] == 'OK':
                self.x = Thread(target = self.reading_thread)
                self.x.start()