drop table if exists User;
drop table if exists Rooms;
drop table if exists Bookings;
drop table if exists Housekeeping;
drop table if exists Payments;
drop table if exists Booking_Dates;

CREATE TABLE User (
user_id          int            not null auto_increment,
user_type        varchar(50)    not null,
name             varchar(100)   not null,
email            varchar(50)    not null,
phone            int            not null,

primary key(user_id)
);

CREATE TABLE Rooms (
room_id           int           not null auto_increment,
room_name         varchar(50)   not null,
room_type         varchar(50)   not null, -- Single, Twin, Double, Deluxe Rooms, Rooms with a View, Suites
bed_size          varchar(50)   not null,
capacity          int           not null,
amentities        varchar(50)   not null, -- a desirable or useful feature or facility of a building or place

primary key(room_id)
);

CREATE TABLE Bookings (
booking_id         int          not null auto_increment,
guest_id           int          not null,
room_id            int          not null,
payment_status     varchar(50)  not null,

primary key(booking_id),
foreign key(guest_id) references User(user_id) on delete cascade,
foreign key(room_id) references Rooms(room_id) on delete cascade
);

CREATE TABLE Booking_Dates (
  booking_id         int          not null,
  check_in           datetime     not null,
  check_out          datetime     not null,
  
  primary key(booking_id),
  foreign key(booking_id) references Bookings(booking_id) on delete cascade
);

CREATE TABLE Housekeeping (
housekeeping_id     int         not null auto_increment,
staff_id            int         not null,
room_id             int         not null,
start_time          datetime,
end_time            datetime,

primary key(housekeeping_id),
foreign key(staff_id) references User(user_id) on delete set null, -- if user was deleted, atleast we know that certain room was cleaned
foreign key(room_id) references Rooms(room_id) on delete set null -- if room was deleted, atleast we know a staff cleaned it, so he/she gets paid :)
);

CREATE TABLE Payments (
payment_id           int         not null auto_increment,
booking_id           int         not null,
payment_type         varchar(50) not null,
payment_amount       int         not null,
payment_date         datetime    not null,

primary key(paymnet_id),
foreign key(booking_id) references Bookings(booking_id) on delete set null -- if booking id was deleted, we keep payment data
);










