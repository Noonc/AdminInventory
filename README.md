### AdminInventory

This is a plugin for Spigot that uses an SQL database to store your survival inventory. It then switches you to creative and clears your inventory. This allows you to then do testing, as well as, admin or dev work (where your stuff would be in the way). Later by use of command again, you can reacquire your items and return to survival.

After returning to survival, the inventory is deleted from the database.

Because it is SQL, you can use it across servers, great for BungeeCord. It is not server-specific, so you can 'create' a survival inventory from one server and retrieve it on another. It saves inventory and returns based on the UUID of the player who executed the command.


Do note, that you have to set up the database beforehand, afterwards tables will be created by the plugin.

## Usage:
/adminon (turns on admin mode, saves inventory, clears it, and sets you to creative)
/adminoff (opposite of adminon)
/admininventory (lists all commangs and usage)
/restartn (Stops the server in 10 seconds with custom message, to be used with a goto Start BAT)
/vanishn (Uses SuperVanish to enable vanish)
/adminreload (Reloads the config)
/adminvanish on/off (Enables or Disables Vanish Mode from ingame)
/adminclear (Clears your current saved inventory)



## Config
```
#Edit all fields as required!
#You may notice that /restartn only stops the server. This command only works (as a restart) if your BAT file you start/run the server with is a goto Start Batch file.
#Essentially it automatically starts the server up again once it closes.
#I implemented this to have a timer of 10 seconds and to message all players online, as well as a Server Closed message.


# The Host/IP
host: '127.0.0.1'

# The port
port: '3306'

#The name of the database used - have to set this up manually
database: 'admininventory'

#The username
user: 'root'

#The password
pass: 'pass'

#Selects whether vanish mode is on. If it is, it will put the player into Vanish (using SuperVanish) when enabling Admin Mode.
vanish: false
```

As noticed in the config above, there is an option to enable vanish. This will turn on/off vanish on the adminon/adminoff commands.

## Permissions: (respectively)
admininventory.adminon
admininventory.adminoff
admininventory.help
admininventory.restart
admininventory.vanish
admininventory.reload
admininventory.vanishset
admininventory.clear

### Warning:
This does not carry over XP.
It saves all data, so for example a Custom Enchants plugin and item NBT Data should work. But I haven't fully tested this.

### TODO:
I hope to add a H2 database option or some other local option.
Performing the command on another player.
I do also hope to add support for XP.
