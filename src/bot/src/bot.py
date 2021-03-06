import socket
import json
import string
import math
import random
import signal
import uuid
import os, sys
from threading import get_ident
from enum import Enum
from threading import Thread
from time import sleep 
from random import randrange

BUFFER_SIZE = 5012
NO_PRINT = True

def blockPrint():
    sys.stdout = open(os.devnull, 'w')

def enablePrint():
    sys.stdout = sys.__stdout__

class ExitCommand(Exception):
    pass

def signal_handler(signal, frame):
    raise ExitCommand()

def bot_function(addr, player_num):
    try:
        print("I'm " + str(get_ident()))
        my_player = Player(_host=addr, _player_num = player_num)
        my_player.start()
        
        if my_player.board.board_height == my_player.board.board_width == 20:
            if player_num == 2:
                if my_player.team == "Red":
                    my_player.move(9,9)
                else:
                    my_player.move(10, 10)
                my_player.pickup()
                my_player.test()
            elif player_num == 3:
                if my_player.team == "Blue":
                    my_player.move(9,9)
                else:
                    my_player.move(10, 10)
                my_player.pickup()
                my_player.test()
            else:
                sleep(0.2)

        while True:
            my_player.leaveGoalArea()
            if my_player.is_carrying_piece:
                my_player.test()
                my_player.wait()
                if my_player.is_carrying_piece:
                    my_player.goAndPlacePiece()
            else:
                my_player.discoverAndTryToPickUpAll()
                
    except ExitCommand:
        # my_player.finish()
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

    def __init__(self, _host = '127.0.0.1', _port = 7654, _player_num = 0):
        self.HOST = _host
        self.PORT = _port
        self.GUID = '0000'
        self.last_action = 'none'
        self.last_status = 'none'
        self.player_num = _player_num  # used for tactics
        self.team_size = 4
        self.goal_area_positions = []
        self.discovered_fields = []
        self.goal_area_position_iter = -1
        self.num_of_denies = 0
        self.last_action_move = None
        print("player num: ", self.player_num)
        self.connect()
        
    def set_guid(self, guid):
        self.GUID = guid

    def reading_thread(self):
        while(True):
            rv = self.recv()
            if rv is None:
                os.kill(os.getpid(), signal.SIGUSR1)
                break
            if(rv['action'] == "end"):
                enablePrint()
                print(rv)
                os.kill(os.getpid(), signal.SIGUSR1)
                break
            
            self.last_action = rv['action']
            self.last_status = rv['status']
            if self.last_status == 'DENIED':
                self.num_of_denies += 1
            else:
                self.num_of_denies = 0
            
            rv = {k: v for k, v in rv.items() if v is not None}  # remove Nones from dict
            print("RECV: ", rv)
 
            if rv['action'] == 'discover':
                self.discovered_fields = rv['fields']
                # for field in rv['fields']:
                    # self.board.set_cell(field['position']['x'], field['position']['y'], field['cell']['distance'])
            elif rv['action'] == 'test':
                if rv['status'] == 'DENIED':
                    self.is_carrying_piece = False
                elif rv['test'] == 'False':
                    self.is_carrying_piece = False
                elif rv['test'] == 'True':
                    self.is_carrying_piece = True
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
        self.last_action_move = self.move_right
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
        self.last_action_move = self.move_left
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
        self.last_action_move = self.move_down
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
        self.last_action_move = self.move_up
        self.send(MoveMessage)
        self.writing = False
        
    def was_denied(self):
        return self.last_status == "DENIED"
    
    def move_random_direction(self):
        print("MOVING RANDOMLY!")
        random_moves = [self.move_up, self.move_down, self.move_left, self.move_right]
        if self.last_action_move in random_moves:
            random_moves.remove(self.last_action_move)
        random.choice(random_moves)()

    def move(self, x, y, go_for_piece = False):
        if x < 0:
            x = 0
        elif x >= self.board.board_width:
            x = self.board.board_width - 1
        elif self.team == "Blue" and y >= self.board.board_height - self.board.goal_area_height:
            y = self.board.board_height - self.board.goal_area_height - 1
        elif self.team == "Red" and y <= self.board.goal_area_height:
            y = self.board.goal_area_height
            
        # if player is discovering he does not need to go to goal area
        if go_for_piece:
            if self.team == "Red" and y >= self.board.board_height - self.board.goal_area_height:
                y = self.board.board_height - self.board.goal_area_height - 1
            if self.team == "Blue" and y <= self.board.goal_area_height:
                y = self.board.goal_area_height

        x_direction = x - self.get_pos_x()
        y_direction = y - self.get_pos_y()
        horizontal_mode = True
        move_index = 0

        while abs(x_direction) > 0 or abs(y_direction) > 0:
            self.wait()
            move_index += 1
            
            if move_index >= self.board.board_height + self.board.board_width + 5:
                self.leave_towards_center()
                return False
            
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
                
            self.wait()
            print("num of denies: ", self.num_of_denies)
            while self.num_of_denies > 2:
                self.move_random_direction()
                self.wait()

            # after_pos = (self.get_pos_x(), self.get_pos_y())
            # TODO: checking if someone blocked, now going like zigzag
            horizontal_mode = not horizontal_mode

            # self.board.show()
            
    def move_horizontal(self):
        next_move = 'none'
        if self.pos_x >= self.allocation_x_end:
            next_move = 'LEFT'
        elif self.pos_x <= self.allocation_x_begin:
            next_move = 'RIGHT'
        else:  # inside the players area
            dist_end = self.allocation_x_end - self.pos_x
            dist_begin = self.pos_x - self.allocation_x_begin
            if dist_end < dist_begin:
                next_move = 'LEFT'
            else:
                next_move = 'RIGHT'
                
        # checking if not blocked below...    
        if next_move == 'LEFT':
            self.move_left()
            self.wait()
            if self.was_denied():
                self.move_right()
        else:
            self.move_right()
            self.wait()
            if self.was_denied():
                self.move_left()

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
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
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
        sleep(0.02)

    def recv(self):
        try:
            message_string = self.socket.recv(BUFFER_SIZE)
            return json.loads(message_string)
        except:
            return None
    
    def get_player_goal_area_positions(self, _player):
        allocation_x_begin = self.player_area_width * _player
        allocation_x_end = allocation_x_begin + self.player_area_width - 1
        if self.team == "Blue":
            goal_area_positions = [(x,y) for x in range(allocation_x_end, allocation_x_begin-1, -1)
                                        for y in range(self.board.goal_area_height)]
        else:
            goal_area_positions = [(x,y) for x in range(allocation_x_end, allocation_x_begin-1, -1)
                for y in range(self.board.board_height-1, self.board.board_height-self.board.goal_area_height-1, -1)]
        return goal_area_positions

    def start(self):
        message = {
            "action" : "connect",
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
            sleep(0.5)
            config = self.recv()
            print("startMessage while")
            print(config)
            
        if config["action"] == "start":
                print(config)
                self.set_board(config["board"]["boardWidth"], config["board"]["taskAreaHeight"] + 2 * config["board"]["goalAreaHeight"], config["board"]["goalAreaHeight"])
                self.set_pos(int(config["position"]["x"]), int(config["position"]["y"]))
                print(config["team"])
                self.set_team(config["team"])
                self.set_guid(config["playerGuid"])
                
                self.team_size = config["teamSize"]
                
                # creating the players horizontal area...
                self.player_area_width = self.board.board_width // self.team_size  # assume we can devide the board equaly here!
                self.allocation_x_begin = self.player_area_width * self.player_num
                self.allocation_x_end = self.allocation_x_begin + self.player_area_width - 1
                self.goal_area_positions = self.get_player_goal_area_positions(self.player_num)
                
                if self.team_size > 1:
                    next_player_num = 0
                    if self.player_num % 2 == 0:
                        next_player_num = self.player_num + 1
                    else:
                        next_player_num = self.player_num - 1
                    print(self.player_num, next_player_num)
                    print(self.goal_area_positions)
                    print(self.get_player_goal_area_positions(next_player_num)[::-1])
                    self.goal_area_positions.extend(self.get_player_goal_area_positions(next_player_num)[::-1])
                
                if self.team_size == 4:
                    if self.player_num == 0:
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(2)[::-1])
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(3)[::-1])
                    elif self.player_num == 1:
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(3)[::-1])
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(2)[::-1])
                    elif self.player_num == 2:
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(0)[::-1])
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(1)[::-1])
                    elif self.player_num == 3:
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(1)[::-1])
                        self.goal_area_positions.extend(self.get_player_goal_area_positions(0)[::-1])
                    
                print(self.goal_area_positions)
                # sleep(400)

                if NO_PRINT:
                    blockPrint()
                signal.signal(signal.SIGUSR1, signal_handler)
                self.x = Thread(target = self.reading_thread)
                self.x.start()
    
    def get_next_free_goal_area_position(self):
        if self.goal_area_position_iter + 1 < len(self.goal_area_positions):
            self.goal_area_position_iter += 1
            return self.goal_area_positions[self.goal_area_position_iter]
        return None
    
    def calculate_distance(self, x, y):
        return math.sqrt( (x - self.pos_x)**2 + (y - self.pos_y)**2 )
    
    def leave_towards_center(self):
        x_dir = self.board.board_width // 3
        y_dir = (self.board.board_height - self.board.goal_area_height*2) // 3
        
        if self.pos_x >= (self.board.board_width // 2):
            x_dir *= -1
        if self.pos_y >= (self.board.board_height // 2):
            y_dir *= -1
            
        # some random direction factor
        if abs(self.pos_x - self.board.board_width) <= 2 and random.uniform(0, 1) > 0.5:
            x_dir *= -1
        if abs(self.pos_x - self.board.board_width) <= 2 and random.uniform(0, 1) > 0.5:
            y_dir *= -1

        self.move(self.pos_x + x_dir, self.pos_y + y_dir)
        
    def pickup_wait(self):
        self.pickup()
        self.wait()
        self.test()
        self.wait()
    
    def generate_dx_dy(self, closest_field):
        dx = closest_field[0] - self.pos_x
        dy = closest_field[1] - self.pos_y
        return (dx, dy)

    def discoverAndTryToPickUpAll(self):
        # number_of_pickups_underneath = 0
        while not self.is_carrying_piece:
            self.discover()
            self.wait()
            if len(self.discovered_fields) == 0:
                self.leaveGoalArea()
            
            min_distance = 9999
            closest_field= (-1, -1)
            standing_on_piece = True
            
            for field in self.discovered_fields:
                if (field['position']['x'] == self.pos_x or field['position']['y'] == self.pos_y) and field['cell']['distance'] > 1:
                    print("NOT STANDING", field)
                    standing_on_piece = False
                if field['cell']['distance'] > -1 and field['cell']['distance'] < min_distance:
                    min_distance = field['cell']['distance']
                    closest_field = (field['position']['x'], field['position']['y'])
            
            (dx, dy) = self.generate_dx_dy(closest_field)
            # print("after discover: ", closest_field, dy, dx, min_distance)
            
            if standing_on_piece:
                # print("STANDING ON THE PIECE")
                self.pickup_wait()
                self.test()
                self.wait()
                if not self.is_carrying_piece:
                    self.leave_towards_center()
                continue
            
            if not self.is_carrying_piece and min_distance == 9999:  # no piece at the board
                # TODO: move to the center - players center so as every bot is responsible
                # for his area
                # self.move_to_center()
                self.leave_towards_center()
                continue
            
            if not self.is_carrying_piece and min_distance == 0:  # piece is just nearby
                self.move(self.pos_x + dx, self.pos_y + dy)
                self.pickup_wait()
            if not self.is_carrying_piece and closest_field[0] == self.pos_x:  # we know where is the piece - vertical movement
                (dx, dy) = self.generate_dx_dy(closest_field)
                self.move(self.pos_x, self.pos_y + dy * (min_distance+1))
                self.pickup_wait()
            if not self.is_carrying_piece and closest_field[1] == self.pos_y:  # we know where is the piece - horizontal movement
                (dx, dy) = self.generate_dx_dy(closest_field)
                self.move(self.pos_x + dx * (min_distance + 1), self.pos_y)
                self.pickup_wait()
            if not self.is_carrying_piece:
                x_di = min_distance // 2
                y_di = x_di
                if x_di + y_di != min_distance:
                    x_di += 1
                self.move(self.pos_x + dx*x_di, self.pos_y + dy*y_di, go_for_piece=True)
        
    def leaveGoalArea(self):
        move_iter = 0
        while(self.get_pos_y()<self.board.goal_area_height):
            move_iter += 1
            if move_iter > 4:
                self.leave_towards_center()
                continue
            self.move_down()
            self.wait()
            if self.was_denied():
                self.move_horizontal()

        while(self.get_pos_y()>=(self.board.board_height-self.board.goal_area_height)):
            move_iter += 1
            if move_iter > 4:
                self.leave_towards_center()
                continue
            self.move_up()
            self.wait()
            if self.was_denied():
                self.move_horizontal()
            

    def goAndPlacePiece(self):
        self.wait()
        if not self.is_carrying_piece:
            return False
        pos = self.get_next_free_goal_area_position()
        if pos is not None: 
            self.move(pos[0], pos[1])
            self.place()
            self.is_carrying_piece = False
            self.wait()