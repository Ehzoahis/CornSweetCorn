(ns adventure.core)
(require '[clojure.string :as str])

(def story 
"You, Matt MacBeckmann, are a average sophomore student in Computer Science. You have enjoyed your mundane college life thus far in the cornfield.

The climate agrees with you, as neither is the summer too hot nor the winter too cold. The corns are the best ones you have ever had. You found a couple of bars as well, where you had fun. 

However, winter this year is something different, the news says a new flu is around, which the fancy smart people had little knowledge of it. 

And weird things start to happen...At beginning it was just a couple of Reddit posts saying there were \"drunk chads\" walking around main quad throwing bricks over random pedestrians. 

But later people are hearing sounds during the midnight at Union, where people saw \"aggresive people with musks\" on a regular basis. Schedules are made that student should not enter union after 10 pm...

You are feeling unsettled and as soon as you finished your final, you started packing your luggage. 

However, it was all too late... Rumor says there are ZOMBIES witnessed on campus yesterday...")

(def help
"[Goal]The goal of this game is to collect enough money while stay alive to purchase a bus ticket leaving Chambana. To get on the bus you will need at least 64 dollars and 30 HP

----Operations----
[Move] move between locations
[Item] a set of operations related to items
  [Item-pick] pick an item from the environment by inputting its index, you might need to pay depending on whether this item is free or not.
  [Item-drop] drop an item to the environment by intputting its index
  [Item-eat] eat an item. Every item in this game is edible, but some might harm your HP
  [Item-inspect] inspect items in different places
    [Item-inspect-environment] inspect items in the environment
    [Item-inspect-inventory] inspect items in your back-pack
----Advanced Operations----
[Money] print current money
[HP] print current hp
[Loc] print current location
[Player] print player status\n")

;;;;;;;set up;;;;;;;;;;;
(def init-incidents
  { 
    :zombie
      {
        :desc "Zombies are seen and the air is smelly. They are moving slowly but never stopping. "
        :hpchange -30
      }
    :police
      {
        :desc "Officers are conducting their responsibility even at presence of the zombies"
        :hpchange 0
      }
    :stink
      {
        :desc "\"Ewww, what kind of smell is that?\""
        :hpchange -5

      }
    :ticket
      {
        :desc "The PCC bus driver: \"Want a ride? Only costs $64 now to take you out of here. But you need to be More than 30 points healthy so that other passengers don't get infected. \" "
        :hpchange 0
      }
    :passport
      {
        :desc "Hey, whose passport is this?"
        :hpchange 0
      } 
  })

(def UIUC
    {
      :morrow-plot 
        {
           :desc "This is a usually empty corn-field at the very center of the University of Illinois at Urbana - Champaign. Upperclassmen often alleged that whoever tried to sneak into the corn field would beapprehended by the police."
           :title "by the morrow plot"
           :incident #{:police}
           :dir {:west :home, :north :police-station} ;:east :FUNK, 
           :contents #{:yummy-corn-bar}
        }
      :eceb
        {
          :desc "It is very STINKY, you will lose 30 points of HP upon every opeartion you conducted here because of the stink."
          :title "in the electrical and computer engineering building"
          :incident #{:stink}
          :dir {:south :altgeld :east :DCL} ; :south-east :illini-union,, :east :siebel
          :contents #{:DB-cafe-sandwich
                      :a-pile-of-ungraded-hws
                      :fake-ID }
        }
      :red-lion
        {
           :desc "This is where all the ZOMBIES are concentrated, BEWARE!"
           :title "in the red lion"
           :dir {:east :home, :north :carle} 
           :incident #{:zombie}
           :contents #{:coarse-vodka}
        }
      :altgeld
        {
          :desc "This is a VERY old building. It is going to collapse at any time"
          :title "in the altgeld hall"
          :incident #{:passport}
          :dir {:north :eceb :south :evo-cafe}  ;:east :illini-union, :north-east :grainger-lib, 
          :contents #{:drafting-compass}
        }
      :DCL
        {
          :desc "This is where some engineering kids take cbtF exams. You may be beaten by ultrapressurized kids who are unhappy about their cbtF exams"
          :title "in the DCL"
          :incident #{:zombie}
          :dir {:north :eceb} ;, :north-east :siebel :south :grainger-lib
          :contents #{}
        }
      :evo-cafe
        {
          :desc "You are really hungry, time to grab some cheap lunch box for $5.99. However, the wait is quite long and their food may be poisonous."
          :title "in the evo-cafe"
          :dir {:north :altgeld, :south :home, :east :carle}
          :incident #{:passport}
          :contents #{:lunch-box
                      :hot-sauce
                      }
        }
      :home
        {
          :desc "HOME SWEET HOME! You are waken up by the morning call from zombies."
          :title "at home"
          :dir {:north :eceb, :south :pcc-bus-stop :west :red-lion} ;:northest :siebel, , :south-west :ARC
          :incident #{}
          :contents #{:deodorant}
        }
      :carle
        {
          :desc "For some reasons, volutary or not you ended up in this very place that can save your life."
          :title "in the carle hospital"
          :incident #{}
          :dir {:west :eceb, :south :home, :east :evo-cafe}
          :contents #{:free-bandaid, 
                      :egregious-hospital-bill 
                      :a-chinese-passport}
        }    
      :pcc-bus-stop
        {
          :desc "You finally made to the pcc bus stop! Buses are waiting there for you! However, have you brought enough money and are you healthy enough to leave?"
          :title "at the peoria charter bus stop."
          :incident #{:ticket}
          :dir {:north :home :west :police-station}
          :contents #{}
        }
      :police-station
        {
          :desc "The police station is where you should go every time you see or experience a crime. Currently the zombie brought much pressure to the officers"
          :title "at the police station"
          :incident #{}
          :dir {:west :carle :north :eceb :east :home}
          :contents #{}
        }
    })

(def init-adv 
  "Initialize the properties of an adventurer."
  { :location :home
    :inventory #{}
    :tick 0
    :seen #{}
    :hp 100
    :money 30
    :capacity 0
  })

(def init-item
  {
    :yummy-corn-bar
      {
        :desc "This is a corn bar, with an tempting glory and pleasant smell, reflecting a golden drop from the sun, but usually you might want to cook it before eating it. "
        :name "Corn "
        :price 0
        :hpchange 20
      }
    :a-pile-of-ungraded-hws
      {
        :desc "This is a huge pile of ungraded home work full of irrational mistakes as well as illegible names and netIDs. You have to risk your life grading this monster. And of course you have to do the sorting as well. " 
        :name "Ungraded HWs "
        :price -50
        :hpchange -10
      }
    :coarse-vodka 
      {
        :desc "A full bottle of transparent liquid, tastes like a pure combustive mixture of alcohol and water, but if you know chemistry, alcohol is a kind of solution. "
        :name "Vodka "
        :price 20
        :hpchange 20
      }
    :fake-ID
      {
        :desc "A fake ID of some random guy above 21 of your race, with a small piece of paper reading \" call him a racist if anyone questioned why the photo looks mismatched \""
        :name "Fake ID "
        :price 100
        :hpchange -20
      }  
    :drafting-compass
      {
        :desc "A drafting compass seemed like of no use, but it did remind you of the younger ages when you and your brother Joe made a slingshot out of a similar and misinterpreted your neighbor's new BMW as \"break my windows\" **You now have a weapon to fight off zombies! Althought is pretty fragile and can be used only once** "
        :name "Compass "
        :price 0
        :hpchange -20
      }
    :lunch-box
      {
        :desc "A very delicious Chinese lunch-box, you can tell that the flavor is intendedly well managed, the chili pepper used is tenderly flirting with your tounge, and the chicken wings make you wonder if they are wings of chickens or wings of angels." 
        :name "Lunchbox "
        :price 5.99
        :hpchange 30
      }
    :egregious-hospital-bill
      {
        :desc "This is a very efficient medicine. After bargaining with the hospital as well as the doctor, you landed on the current price. Unfortunately you do not have a valid student insurance. "
        :name "very efficient medicine "
        :price 1000
        :hpchange 100        
      }

    :free-bandaid 
      {
        :desc "This is a very useful bandaid in the occasion of a papercut or staple poke. However cannot deal with a mental scar"
        :name "Bandaid "
        :price 0
        :hpchange 10
      }

    :a-chinese-passport 
      {
        :desc "This is a passport of some random Chinese guy with a name Sizhang Xu, by sending him an email you managed to contact him, and he asked you to bring the passport to Altgeld, promising that he will give you some money to show his gratitude" ;TODO : ****
        :name "Chinese Passport "
        :price 0
        :hpchange 0
      }
    :deodorant
      {
        :desc "The deodorant that will allow you to safely enter ECEB!" 
        :name "deodorant"
        :price 0
        :hpchange -100
      }
    :hot-sauce
      {
        :desc "A bowl of hot sause that will warm your heart, and hp" ;TODO : ****
        :name "Hot sause"
        :price 0
        :hpchange 10
      }

    :DB-cafe-sandwich
      {
        :desc "This is a sandwich with weirdly high price of 10 dollar, it's ugly but essential in situations like this. "
        :name "Cold Sandwich "
        :price 10
        :hpchange 10
      }    
  })

;;;;;;;;driving codes;;;;;;;;;;

(defn match [pattern input]
  (loop [pattern pattern
         input input
         vars '()]
    (cond (and (empty? pattern) (empty? input)) (reverse vars)
          (or (empty? pattern) (empty? input)) nil
          (= (first pattern) "@")
            (recur (rest pattern)
                 (rest input)
                 (cons (first input) vars))
          (= (first pattern) (first input))
             (recur (rest pattern)
                    (rest input)
                    vars)
          :fine-be-that-way nil)))

(defn canonicalize
  "Given an input string, strip out whitespaces, lowercase all words, and convert to a vector of keywords."
  [input]
    (for [kws (-> input
                  str/lower-case
                  (str/replace #"[?!.]" "")
                  (str/split #" +"))]
        (keyword kws)))

(defn confused [state]
  (println "I don't know what you mean.") 
  state)

(defn react
  "Given a state and a canonicalized input vector, search for a matching phrase and call its corresponding action.
  If there is no match, return the original state and result \"I don't know what you mean.\""
  [state input-vector]
  (let [base (get-in state [:runtime])]
    (loop [idx 0]
      (if (>= idx (count base))
        (confused state)
        (if-let [vars (match (base idx) input-vector)]
          (apply (base (inc idx)) state vars)
          (recur (+ idx 2)))))))

(defn console-ui []
  "Interface for repl"
  (print ">")
  (flush)
  (read-line))

;;;;;;;;;incidents;;;;;;;;;;
(defn zombie [state]
  (let [curri (get-in state [:adventurer :inventory])
        hp (get-in state [:adventurer :hp])  ]
    (if (contains? curri :drafting-compass)
      (update-in state [:adventurer :inventory] #(disj % :drafting-compass))
      (if (contains? curri :hot-source)
        (update-in state [:adventurer :inventory] #(disj % :hot-source))
        (if (contains? curri :course-vodka)
          (update-in state [:adventurer :inventory] #(disj % :course-vodka))
          (do (println "\nOuch! You got bitten by a zombie!!")
              (println "\n**You lose 5 points hp**")
              (assoc-in state [:adventurer :hp] (- hp 5))))))))

(defn ticket [state]
  "Check whether the player has the money to buy a ticket."
  (let [current-money (get-in state [:adventurer :money])
        currhp (get-in state [:adventurer :hp])]
    (if (and (> current-money 64) (> currhp 30))
      (do (assoc-in state [:adventurer :money] (- current-money 64))
          (println "\nCongratulations, you won!!!! You can type [Q]uit to quit the game, but you are always more than welcome to explore the campus a little more")
          state)
      (do (println "\nYou don't have enough money right now...")
          state)
      )))

(defn stink [state]
  (let [curri (get-in state [:adventurer :inventory])
        currhp (get-in state [:adventurer :hp])]
    (if (contains? curri :deodorant)
        (do (println "\nYou use your deoderant, the air becomes clean again.\n\n**Deoderant is removed from your inventory**")
        (-> state (update-in [:adventurer :inventory] #(disj % :deodorant))
                  (update-in [:map :eceb :incident] #(disj % :stink))
                  (update-in [:adventurer :capacity] #(dec %))))
      (do (println "")
          (println "**you lose 30 points hp**")
          (assoc-in state [:adventurer :hp] (- currhp 30))))))

(defn police [state]
  (let [current-money (get-in state [:adventurer :money])
        curri (get-in state [:adventurer :inventory])]
    (if (contains? curri :coarse-vodka)
      (if (not (contains? curri :fake-ID))
        (do (println "\nUh-oh, you are underaged!! You lost your vodka.")
            (println "\n**You receive a fine which costs you 20 dollars**")
            (-> state
            (assoc-in  [:adventurer :money] (- current-money 20))
            (update-in  [:adventurer :inventory] #(disj % :coarse-vodka))))
        (println "The officer checked your ID and left.")))))

(defn passport [state]
  (let [current-money (get-in state [:adventurer :money])
        curri (get-in state [:adventurer :inventory])]
    (if (contains? curri :a-chinese-passport   )
      (do (println "\"Thank you so much!! This is for you.\" --Sizhang Xu\n\n**money increased by 100**")
          (-> state
          (assoc-in [:adventurer :money] (+ current-money 100))
          (update-in [:adventurer :inventory] #(disj % :a-chinese-passport))))
      (do (println "This is where the story of passport happens.")
          state))))

;;;;;;;playerable operations;;;;;;;;;;
(defn status [state]
  "Print out the location info"
  (let [location (get-in state [:adventurer :location])
        the-map (:map state)]
    (println (str "You are " (-> the-map location :title) ".\n"))
    (when-not (contains? (get-in state [:adventurer :seen]) location)
      (println (-> the-map location :desc)))
    (update-in state [:adventurer :seen] #(conj % location))))

(defn available_dir [state]
  (let [location (get-in state [:adventurer :location])
        the_map (:map state)
        dest1 (get-in state [:map location :dir :east])
        dest2 (get-in state [:map location :dir :south])
        dest3 (get-in state [:map location :dir :west])
        dest4 (get-in state [:map location :dir :north])]
  (if-not (nil? dest1)
    (println (str "In the direction of EAST, you can go to the " (name dest1))))
  (if-not (nil? dest2)
    (println (str "In the direction of SOUTH, you can go to the " (name dest2))))
  (if-not (nil? dest3)
    (println (str "In the direction of WEST, you can go to the " (name dest3))))
  (if-not (nil? dest4)
    (println (str "In the direction of NORTH, you can go to the " (name dest4))))
  state))  

(defn go [state dir]
  "Decide where to go"
  (let [location (get-in state [:adventurer :location])
        dest (get-in state [:map location :dir (keyword dir)])]
    (if (nil? dest)
      (do (println "You hit an invisible wall.") state)
      (assoc-in state [:adventurer :location] dest))))

(defn print-hp [state]
  "Check hp from the adventurer."
  (let [current-hp (get-in state [:adventurer :hp])]
    (do (print (str "HP:" current-hp "/100  "))))
  state)

(defn print-money [state]
  "check hp from the adventurer."
  (let [current-money (get-in state [:adventurer :money])]
    (do (print (str "MONEY:" current-money "  "))))
  state)

(defn check-death [state]
  (let [hp (get-in state [:adventurer :hp])]
    (if (> hp 0) false true)))

;;;;;;;;;item operations;;;;;;;;;
(defn inspect-back-pack [state]
  "Print items in the bag"
  (let [capacity (get-in state [:adventurer :capacity])
        item-exist (get-in state [:adventurer :inventory])
        item-list (into '() item-exist)]
    (println (str "capacity: " capacity "/10"))
    (if (empty? item-exist)
      (println "Your bag is empty")
      (println (apply str "You have:\n| " (vec (for [item item-list] (str (name item) " | ")))))))
  state)

(defn inspect-room [state]
  "Print items in the room"
  (let [location (get-in state [:adventurer :location])
        item-exist (get-in state [:map location :contents])
        item-list (into '() item-exist)]
    (if (empty? item-exist)
      (println "Nothing left here.")
      (println (apply str "| " (vec (for [item item-list] (str (name item) " | ")))))))
  state)

(defn inspect-item [state index]
  "Inspect items"
  (let [location (get-in state [:adventurer :location])
        item-list (into '() (get-in state [:map location :contents]))
        max-item-idx (- (count item-list) 1)
        idx (read-string (name index))]
    (cond (not (number? idx)) (println "Index must be a number!")
          (> idx max-item-idx) (println "Not available!")
          :else (let [item (nth item-list idx)]
                  (println (str "Item Name: " (get-in state [:items item :name]) "\n"
                                "Description: " (get-in state [:items item :desc]) "\n" 
                                "Price: " (get-in state [:items item :price]))))))
  state)

(defn inspect-bag-item [state index]
  "Inspect items in the bag"
  (let [item-list (into '() (get-in state [:adventurer :inventory]))
        max-item-idx (- (count item-list) 1)
        idx (read-string (name index))]
    (cond (not (number? idx)) (println "Index must be a number!")
          (> idx max-item-idx) (println "Not available!")
          :else (let [item (nth item-list idx)]
                  (println (str "Item Name: " (get-in state [:items item :name]) "\n"
                                "Description: " (get-in state [:items item :desc]) "\n" 
                                "HP Effect: " (get-in state [:items item :hpchange]))))))
  state)

(defn eat-item [state index]
  "Eat item"
  (let [item-list (into '() (get-in state [:adventurer :inventory]))
        max-item-idx (- (count item-list) 1)
        pack-cap (get-in state [:adventurer :capacity])
        hp (get-in state [:adventurer :hp])
        idx (read-string (name index))]
    (cond (not (number? idx)) (do (println "Index must be a number!") state)
          (> idx max-item-idx) (do (println "Not available!") state)
          :else (let [item (nth item-list idx)
                      hp-new (cond (> (+ hp (get-in state [:items item :hpchange])) 100) 100
                                   (< (+ hp (get-in state [:items item :hpchange])) 0) 0
                                   :else (+ hp (get-in state [:items item :hpchange])))]
                  (-> state (update-in [:adventurer :inventory] #(disj % item))
                            (assoc-in [:adventurer :hp] hp-new)
                            (assoc-in [:adventurer :capacity] (- pack-cap 1)))))))

(defn pick-up-item [state index]
  "Pick item"
  (let [location (get-in state [:adventurer :location])
        item-list (into '() (get-in state [:map location :contents]))
        max-item-idx (- (count item-list) 1)
        pack-cap (get-in state [:adventurer :capacity])
        money (get-in state [:adventurer :money])
        idx (read-string (name index))]
      (cond (not (number? idx)) (do (println "Index must be a number!") state)
            (= pack-cap 10) (do (println "Bag is full! Drop something!") state)
            (> idx max-item-idx) (do (println "Not available!") state)
            :else (let [item (nth item-list idx)
                        price (get-in state [:items item :price])]
                          (cond (< money price) (println "Get more money!")
                                (= item :a-pile-of-ungraded-hws) (do (println "\nYou risk your life to grade the homework and get some money.\n\n**money increased by 50**\n**hp decreased by 10**")
                                                                      (-> state (assoc-in [:adventurer :money] (- money price))
                                                                                (update-in [:adventurer :inventory] #(conj % item))
                                                                                (update-in [:map location :contents] #(disj % item))
                                                                                (assoc-in [:adventurer :capacity] (+ pack-cap 1))
                                                                                (eat-item (keyword (str pack-cap)))))                                                                                            
                                :else (-> state (assoc-in [:adventurer :money] (- money price))
                                                (update-in [:adventurer :inventory] #(conj % item))
                                                (update-in [:map location :contents] #(disj % item))
                                                (assoc-in [:adventurer :capacity] (+ pack-cap 1))))))))

(defn drop-item [state index]
  "Drop item"
  (let [location (get-in state [:adventurer :location])
        item-list (into '() (get-in state [:adventurer :inventory]))
        max-item-idx (- (count item-list) 1)
        pack-cap (get-in state [:adventurer :capacity])
        idx (read-string (name index))]
    (cond (not (number? idx)) (println "Index must be a number!")
          (> idx max-item-idx) (println "Out of range!")
          :else (let [item (nth item-list idx)]
              (-> state (update-in [:adventurer :inventory] #(disj % item))
                        (update-in [:map location :contents] #(conj % item))
                        (assoc-in [:adventurer :capacity] (- pack-cap 1)))))))

(defn print-current-status [state]
  "Print adventurer"
  (println "")
  (-> state print-hp
            print-money
            inspect-back-pack))

(defn pick [state]
  (do (println "\n-----------------------------------")
      (println "Enter the zero-based index [0-9] of the item you want to pick."))
  (let [input (console-ui)
        idx (first (canonicalize input))
        output (react state [:take idx])]    
        (do (println "\n-----------------------------------")
            (println "\n**After picking up the item, your money is" (get-in state [:adventurer :money]) "!**")
            (println "\nCurrent location:")
            (inspect-room output)
            (println "Inventory:")
            (inspect-back-pack output)
            output)))

(defn drop-op [state]
  (do (println "\n-----------------------------------")
      (println "Enter the zero-based index [0-9] of the item you want to drop."))
  (let [input (console-ui)
        idx (first (canonicalize input))
        output (react state [:drop idx])]
    (do (println "\n-----------------------------------")
        (println "\nCurrent location:")
        (inspect-room output)
        (println "Inventory:")
        (inspect-back-pack output)
        output)))

(defn eat [state] 
  (do (println "\n-----------------------------------")
      (println "Enter the zero-based index [0-9] of the item you want to eat."))
  (let [input (console-ui)
        idx (first (canonicalize input))
        output (react state [:eat idx])]
    (do (println "\n-----------------------------------")
        (println "\n**After having a nice meal, your hp is" (get-in state [:adventurer :hp]) "!**")
        (println "\nCurrent location:")
        (inspect-room output)
        (println "Inventory:")
        (inspect-back-pack output)
        output)))

(defn ins-room [state]
  (do (println "\n-----------------------------------")
      (println "Enter the zero-based index [0-9] of the item you want to inspect."))
  (let [input (console-ui)
        idx (first (canonicalize input))]
    (if (= (first (canonicalize "")) idx) 
      (println "Please enter a number."))
      (do (println "\n-----------------------------------") (react state [:show idx]))))

(defn ins-inventory [state]
  (do (println "\n-----------------------------------")
      (println "Enter the zero-based index [0-9] of the item you want to inspect."))
  (let [input (console-ui)
        idx (first (canonicalize input))]
    (if (= (first (canonicalize "")) idx) 
      (println "Please enter a number."))
      (do (println "\n-----------------------------------") (react state [:show idx]))))

(defn inspect [state] 
  (loop [state state] 
    (do (println "\n-----------------------------------")
        (println "Where do you want to inspect? [E]nvironment/[I]nventory/[C]ancel"))
    (let [input (console-ui)
          loc (first (canonicalize input))]
      (cond (or (= (first (canonicalize input)) :e) (= (first (canonicalize input)) :environment)) (recur (ins-room state))
            (or (= (first (canonicalize input)) :i) (= (first (canonicalize input)) :inventory)) (recur (ins-inventory state))
            :else state))))

;;;;;;;;;room operations;;;;;;;;
(defn examine-incident [state]
  "Check incident"
  (let [location (get-in state [:adventurer :location])
        incident (get-in state [:map location :incident])]
    (cond (empty? incident) state
          (contains? incident :zombie) (do (println (get-in init-incidents [:zombie :desc])) (zombie state))
          (contains? incident :ticket) (do (println (get-in init-incidents [:ticket :desc])) (ticket state))
          (contains? incident :stink) (do (println (get-in init-incidents [:stink :desc])) (stink state))
          (contains? incident :police) (do (println (get-in init-incidents [:police :desc])) (police state))
          (contains? incident :passport) (do (println (get-in init-incidents [:passport :desc])) (passport state))
          :else state)))

(defn check-death [state]
  (let [hp (get-in state [:adventurer :hp])]
    (if (< hp 1) true false)))

(defn go-deluxe [state dir]
  (println "\n-----------------------------------")
  (-> state
      (go dir)
      (status)
      (examine-incident)
      (print-current-status)))

(defn move [state]
  (println "")
  (println "-----------------------------------")
  (println "These are available movements:")
  (available_dir state)
  (println "")
  (println "-----------------------------------")
  (println "Which direction do you want to go? ([E]ast/[W]est/[S]outh/[N]orth/[C]ancel)")
  (let [input (console-ui)]
      (cond (or (= (first (canonicalize input)) :e) (= (first (canonicalize input)) :east)) (react state [:go :east]) 
            (or (= (first (canonicalize input)) :s) (= (first (canonicalize input)) :south)) (react state [:go :south])
            (or (= (first (canonicalize input)) :w) (= (first (canonicalize input)) :west)) (react state [:go :west]) 
            (or (= (first (canonicalize input)) :n) (= (first (canonicalize input)) :north)) (react state [:go :north]) 
            :else (do (println "OK, cancelled")
                      state))))

(defn item [state]
  (println "\n-----------------------------------")
  (println "Current location:")
  (inspect-room state)
  (println "Inventory:")
  (inspect-back-pack state)
  (loop [state state]
    (println "")
    (println "-----------------------------------") 
    (println "What operation do you want to do? [P]ick/[D]rop/[E]at/[I]nspect/[C]ancel")
    (let [input (console-ui)]
      (cond (or (= (first (canonicalize input)) :p) (= (first (canonicalize input)) :pick)) (recur (pick state))
            (or (= (first (canonicalize input)) :d) (= (first (canonicalize input)) :drop)) (recur (drop-op state))
            (or (= (first (canonicalize input)) :e) (= (first (canonicalize input)) :eat)) (recur (eat state))
            (or (= (first (canonicalize input)) :i) (= (first (canonicalize input)) :inspect)) (recur (inspect state))
            (or (= (first (canonicalize input)) :c) (= (first (canonicalize input)) :cancel)) (do (println "OK, cancelled") state)
            (or (= (first (canonicalize input)) :q) (= (first (canonicalize input)) :quit)) state
            :else (do (confused state) (recur state))))))

;;;;;;;repl and main;;;;;;;;
(def initial-env [  [:go "@"] go-deluxe
                    [:loc] status
                    [:inspect :room] inspect-room
                    [:check :in] inspect-back-pack
                    [:hp] print-hp
                    [:money] print-money
                    [:dir] available_dir
                    [:show "@"] inspect-item
                    [:show :bag "@"] inspect-bag-item
                    [:take "@"] pick-up-item
                    [:drop "@"] drop-item
                    [:eat "@"] eat-item
                    [:print] print-current-status
                    [:player] print-current-status
                  ])

(defn repl [env]
  (do (println "----Corn Sweet Corn----\n")
      (println "Welcome to \"Corn Sweet Corn\", by Haozhe Si, Jialiang Xu and Hongshuo Zhang\n")
      (println "----Input anything to start, or directly hit enter to quit----")
      (let [input (console-ui)
            state-init {:map UIUC :items init-item :adventurer init-adv :incident init-incidents}]
        (if-not (empty? input)
          (do (println "")
              (println "-----------------------------------")
              (println story)
              (println "")
              (println "-----------------------------------")             
              (status state-init)
              (print-current-status state-init)
              (loop [state state-init]
                (println "")
                (println "-----------------------------------")
                (println "What do you want to do? ([M]ove/[I]tem/[H]elp/[Q]uit)")
                (let [input (console-ui)]
                  (cond (or (= (first (canonicalize input)) :h) (= (first (canonicalize input)) :help)) (do (print help) (recur state))
                        (or (= (first (canonicalize input)) :q) (= (first (canonicalize input)) :quit)) nil
                        (or (= (first (canonicalize input)) :m) (= (first (canonicalize input)) :move)) (let [output (move (assoc-in state [:runtime] env))] (if (check-death output) (println "\n----You are dead. Game over!----") (recur (if-not (nil? output) output state))))
                        (or (= (first (canonicalize input)) :i) (= (first (canonicalize input)) :item)) (let [output (item (assoc-in state [:runtime] env))] (if (check-death output) (println "\n----You are dead. Game over!----") (recur (if-not (nil? output) output state))))
                        :else (let [output (react (assoc-in state [:runtime] env) (canonicalize input))]
                                (if (check-death output) (println "\n----You are dead. Game over!----") (recur (if-not (nil? output) output state))))))))))))

(defn -main
  "Start the REPL with the initial environment."
  []
  (repl initial-env)
  )
