globals
[ pointer
  brush-radius
  current-file     ; stored filename of the nim file
  current-png-file ; stored filename of the png file
  small-brush-size ; pre-determined brush sizes
  big-brush-size
  undo-point  ; current slice of the undo history being displayed
  undo-length ; current length of the undo history
  clicks ; used to track mouse clicks
  io?    ; used to stop the drawing tool during file i/o.
]

breeds
[ pointers ; the painting tool
  markers  ; markers show the shape as you draw it
]

patches-own 
[ sample-color ; could also be called "temp-color"
  unpainted? ; helps prevent infinte recursion loops
  history    ; the undo-history of the patch
]

pointers-own
[ start-x 
  start-y
  marker-count
  moved?
  old-x
  old-y
]

markers-own
[ start-x
  start-y
  marker-count
  marker-x
  marker-y
  order
]

to startup
    setup
end

to setup
    set io? false
    create-pointer-turtle
    ask patches
    [ set unpainted? true 
      set pcolor canvas
    ]
    history-record
end

to create-pointer-turtle
    set small-brush-size 5
    set big-brush-size 10
    if big-brush-size > screen-edge-x
    [ set big-brush-size screen-edge-x ]

    ;; make sure there are no left-over markers
    clear-turtles

    ;; create pointer turtle
    create-custom-pointers 1
    [ setxy 0 0
      set clicks 0
      set pointer self
      set moved? false
      set shape "oc"
      set size big-brush-size
    ]
end

to main-drawing-tool-loop
    if not is-turtle? pointer or pointer = nobody [  without-interruption [ create-pointer-turtle ] ]
    if not is-boolean? io? [ set io? false ]
    if io? [set io? false stop ] ; stop main loop button during file-io to speed things up.
      ifelse tool = "brush"               [ do-tool-input-brush     ]  ; 1
    [ ifelse tool = "lines"               [ do-tool-input-lines     ]  ; 2
    [ ifelse tool = "fills"               [ do-tool-input-fills   0 ]  ; 3
    [ ifelse tool = "fill-shades"         [ do-tool-input-fills   1 ]  ; 4
    [ ifelse tool = "boxes"               [ do-tool-input-boxes   0 ]  ; 5
    [ ifelse tool = "frames"              [ do-tool-input-boxes   1 ]  ; 6
    [ ifelse tool = "rings"               [ do-tool-input-circles 0 ]  ; 7
    [ ifelse tool = "circles"             [ do-tool-input-circles 1 ]  ; 8
    [ ifelse tool = "pick-color"          [ do-tool-input-pick-color   ]  ; 9
    [ ifelse tool = "change-color"        [ do-tool-input-change-color ]  ;10
    [ stop ] ; note: need one close bracket for each option
    ]]]]] ]]]]
end


to set-brush-radius
    set brush-radius int ( ( brush-width * .5 ))
end

to-report mouse-clicked?
    ; tests and tracks the progress of a mouse click.
    ; reports true when the click is completed
    ; it always takes two passes through this function to detect a click,
    ; so code that uses this function must be in a loop,
    ; and must be ok with the click-detection taking two passes!
    ifelse clicks = 1 and not mouse-down?
    [ set clicks 0
      report true 
    ]
    [ if clicks = 0 and mouse-down?
      [ set clicks 1 ]
      report false
    ]
end

to do-tool-input-brush
     locals
     [ brush-x
       brush-y
     ]   

    ask pointer
    [ if shape != "tip" [ set shape "tip" ]
      if size != small-brush-size [ set size small-brush-size ]
      if color != brush-color [ set color brush-color ]
      set brush-x (round mouse-xcor)
      set brush-y (round mouse-ycor)
      set moved? old-x != brush-x or old-y != brush-y
      if moved?
      [ set old-x xcor
        set old-y ycor
        setxy brush-x brush-y
        set unpainted? true
      ]
      if mouse-down? and unpainted?
      [ if clicks = 0
        [ ; this is the beginning of a brush-stroke
          set clicks 1
        ]
        brush-paint
      ]
      if not (mouse-down? or unpainted?)
      [ set unpainted? false ]
      if not mouse-down? and clicks = 1
      [ ; this is the end of a brush-stroke, record undo
        set clicks 0
        history-record
      ]
    ]
end

to do-tool-input-lines
      locals
      [ brush-x
        brush-y
        index
      ]
      ask pointer
      [ ; set pointer shape, if not set
        if shape != "ch" [ set shape "ch" ]
        ; set pointer size, if not set
        if size != big-brush-size [ set size big-brush-size ]
        ; set pointer color, of not set
        if color != brush-color [ set color brush-color ]
        ; remember mouse location
        set brush-x (round mouse-xcor)
        set brush-y (round mouse-ycor)
        ; note if mouse is different from pointer location
        set moved? old-x != brush-x or old-y != brush-y
        ; if mouse has moved, move pointer, too
        if moved?
        [ set old-x xcor
          set old-y ycor
          setxy brush-x brush-y
        ]
        ; mouse-button event handler
        ; line drawing uses click-drag-release
        ; just like shapes editor!
        ifelse mouse-down?
        [ ; on mouse-button-down, start line
          if clicks = 0
          [ ; beginning of click-drag-release sequence
            ; remember start of line
            set start-x brush-x
            set start-y brush-y
            ; calculate number of markers
            set marker-count 5 + 2 * int ( log ( screen-size-x * screen-size-y ) 10)
            ; create markers
            set index 0
            ; create markers
            hatch marker-count
            [ set breed markers
              ifelse markers?
              [ set shape "bx" ]
              [ set shape "line" ]
              set size 1
              set order index / marker-count
              set index index + 1
              ; note: since turtles created inside hatch
              ; run "without-interruption"
              ; each marker gets a unique value for index
            ]
            ; promote click 
            set clicks 1
            ; ensure markers are drawn next, set move? true
            set moved? true
          ]
          if clicks = 1 and moved?
          [ ; mouse-drag in progress
            ; place markers and color to track current mouse / line location
            ask markers
            [ set xcor start-x + order * ( brush-x - start-x )
              set ycor start-y + order * ( brush-y - start-y)
              set color inverse? pcolor
              if distancexy-nowrap start-x start-y > 0
              [ set heading towardsxy-nowrap start-x start-y ]
            ]
          ]
        ]
        [ ; mouse-release, set and draw line to current location 
          if clicks = 1
          [ paint-line start-x start-y brush-x brush-y
            ask markers [ die ]
            set clicks 0
            history-record
          ]
        ] ; if mouse-down?
      ] ; ask pointer
end ; do-lines

to do-tool-input-circles [ mode ]
    locals
    [ brush-x
      brush-y
      index
      radius
    ]
      ask pointer
      [ if shape != "chc" [ set shape "chc" ]
        if size != big-brush-size [ set size big-brush-size ]
        if color != brush-color [ set color brush-color ]
        set brush-x (round mouse-xcor)
        set brush-y (round mouse-ycor)
        set moved? old-x != brush-x or old-y != brush-y
        if moved?
        [ set old-x xcor
          set old-y ycor
          setxy brush-x brush-y
        ]
        ifelse mouse-down?
        [ if clicks = 0
          [ set start-x brush-x
            set start-y brush-y
            set marker-count 20 ; 4 + int ( log ( screen-size-x * screen-size-y ) 10)
            set index 0
            hatch marker-count
            [ set breed markers
              ifelse markers?
              [ set shape "bx" ]
              [ set shape "circum" ]
              set size 1
              set order index / marker-count * 360
              set index index + 1
            ] ; hatch
            set clicks 1
            set moved? true
          ] ; if clicks = 0
          if clicks = 1 and moved?
          [ set radius distancexy-nowrap start-x start-y
            ask markers
            [ set marker-x start-x + radius * sin order
              set marker-y start-y + radius * cos order
              if abs marker-x > screen-edge-x 
              [ set marker-x screen-edge-x * sign marker-x ]
              if abs marker-y > screen-edge-y
              [ set marker-y screen-edge-y * sign marker-y ]
              setxy marker-x marker-y
              set color inverse? pcolor
              if radius > 0
              [ set heading towardsxy-no-wrap start-x start-y ]
            ] ; ask markers
         ] ; clicks = 1 and moved?
       ] ; ifelse mouse-down?
       [ ; mouse is not down
         if clicks = 1
         [ ; mouse-button released
           ; draw circle indicated
           ifelse mode = 0
           [ circle-paint-edge start-x start-y brush-x brush-y ]
           [ circle-paint-solid start-x start-y brush-x brush-y ]
           set clicks 0
           ask markers
           [ die ]
           history-record
        ] ; if clicks = 1
       ] ; else of ifelse mouse-down?
     ] ; ask pointer
end ; do-circles

to do-tool-input-fills [ mode ]
    locals
    [ brush-x
      brush-y
    ]
    ask pointer
    [ if shape != "ch" [ set shape "ch" ]
      if size != big-brush-size [ set size big-brush-size ]
      if color != brush-color [ set color brush-color ]
      set brush-x (round mouse-xcor)
      set brush-y (round mouse-ycor)
      set moved? old-x != brush-x or old-y != brush-y
      if moved?
      [ set old-x xcor
        set old-y ycor
        setxy brush-x brush-y
      ]
      if mouse-clicked? 
      [ ask patches with [ unpainted? = false ]
        [ set unpainted? true ]
        ifelse mode = 0
        [ fill-solids pcolor ]
        [ fill-shades pcolor ]
        history-record
      ]
    ]
end ; do-fills


to do-tool-input-change-color
    locals
    [ brush-x
      brush-y
      clicked
    ]
    ask pointer
    [ if shape != "cp" [ set shape "cp" ]
      if size != 10 [ set size 10 ]
      ; if color != brush [ set color brush ]
      set brush-x (round mouse-xcor)
      set brush-y (round mouse-ycor)
      if old-x != brush-x or old-y != brush-y
      [ set old-x xcor
        set old-y ycor
        setxy brush-x brush-y
      ]
      set color pcolor
      if mouse-clicked?
      [ ask patches with [ pcolor = color-of myself ]
        [ set pcolor brush-color ]
        history-record
      ]
    ]
end ; do-change


to do-tool-input-boxes [ mode ]
    locals
    [ brush-x
      brush-y
      index
    ]
      ask pointer
      [ if shape != "chbx" [ set shape "chbx" ]
        if size != big-brush-size [ set size big-brush-size ]
        if color != brush-color [ set color brush-color ]
        set brush-x (round mouse-xcor)
        set brush-y (round mouse-ycor)
        set moved? old-x != brush-x or old-y != brush-y
        if moved?
        [ set old-x xcor
          set old-y ycor
          setxy brush-x brush-y
        ]
        ifelse mouse-down?
        [ if clicks = 0
          [ set start-x brush-x
            set start-y brush-y
            set marker-count ( 32 )
            set index 0
            hatch marker-count
            [ set breed markers
              set color inverse? pcolor
              ifelse markers?
              [ set shape "bx" ]
              [ set shape "line" ]
              set size 1
              set order index
              if order >= 16 [ set heading 90 ]
              set index index + 1
            ]
            set clicks 1
            set moved? true
          ]
          if clicks = 1 and moved?
          [ ask markers
            [ ifelse order < 16
              [ set xcor start-x + order mod 2 * ( brush-x - start-x )
                set ycor start-y + int( order / 2 ) / 8.0 * ( brush-y - start-y )
              ]
              [ set xcor start-x + int( (order - 16) / 2) / 8.0 * ( brush-x - start-x )  
                set ycor start-y + order mod 2 * ( brush-y - start-y )
              ]
              set color inverse? pcolor  
            ]
          ]
        ]
        [ if clicks = 1
          [ ifelse mode = 0
            [ paint-frame start-x start-y brush-x brush-y ]
            [ box-paint   start-x start-y brush-x brush-y ]
            ask markers
            [ die ]
            set clicks 0
            history-record
          ]
        ]
      ]
end

to do-tool-input-pick-color
    locals
    [ brush-x
      brush-y
    ]
    ask pointer
    [ if shape != "cp" [ set shape "cp" ]
      if size != 10 [ set size 10 ]
      ; if color != brush [ set color brush ]
      set brush-x (round mouse-xcor)
      set brush-y (round mouse-ycor)
      if old-x != brush-x or old-y != brush-y
      [ set old-x xcor
        set old-y ycor
        setxy brush-x brush-y
      ]
      set color pcolor
      if mouse-clicked?
      [ set brush-color pcolor ]
    ]
end ; do-pick

to brush-paint
    locals [ result mypatches mypxcor mypycor]
    ifelse brush-width = 1
    [ stamp-efx
      set unpainted? false
    ]   
    [ set-brush-radius
      set mypatches patches in-radius-nowrap brush-radius
      paint-efx mypatches
      ask mypatches [ set unpainted? false ]
    ]
end

to paint-line [ x1 y1 x2 y2 ] ; input end-points of line
; effects: "1 solid" "2 dappled" "3 undapple" "4 darken" "5 lighten" "6 blend"
    locals [ b-left b-top       ;
             b-right b-bottom   ; corners of the bounding box
             ex-left ex-top     ;
             ex-right ex-bottom ; corners of the bounding box, EXpanded by brush-radius
             mybox           ; patches within the expanded bounding box
             myline          ; patches directly along the line
             myendpoints     ; patches in radius brush-radius of the end-points
             mystroke        ; patches within brush-radius of the line
             mypatches       ; patches in mybox within brushwith of myline
                             ; union of mystroke and myendpoints
              
             ; equation for a line, in terms of y : y = mx + b
             m         ; slope, rise / run, y-delta / x-delta, aka m 
             b        ; y intercept, aka b
             ; same line, in terms of x: x = ny + a
             n         ; slope, run / rise, x-delta / y-delta,  aka n  
             a         ; x intercept, aka a
           ]
  
    set-brush-radius   
    ifelse x1 <= x2
    [ set b-left x1      set b-right x2 ]
    [ set b-left x2      set b-right x1 ]
    ifelse y1 <= y2
    [ set b-top y1      set b-bottom y2 ]
    [ set b-top y2      set b-bottom y1 ]
    
    set ex-left   b-left   - brush-radius
    set ex-top    b-top    - brush-radius
    set ex-right  b-right  + brush-radius
    set ex-bottom b-bottom + brush-radius

    ; set mybox  patches with [     pxcor >= ex-left and pxcor <= ex-right
     ;                         and pycor >= ex-top  and pycor <= ex-bottom ]

    ifelse b-top = b-bottom or b-left = b-right
    [
      set mypatches patches-from (patches with [     pxcor >= b-left
                              and pxcor <= b-right
                              and pycor >= b-top
                              and pycor <= b-bottom
                            ]) [ patches in-radius-nowrap brush-radius ]
    ]
    [ set m ( y1 - y2 ) / ( x1 - x2 )
      set n ( x1 - x2 ) / ( y1 - y2 )
      set b  y1 - ( m * x1 )
      set a  x1 - ( n * y1 )
      
      ; find patches that lie along the line
      set mypatches patches-from (patches with
                 [     pxcor >= b-left and pxcor <= b-right
                   and pycor >= b-top  and pycor <= b-bottom 
                   and (   pycor = round ( m * pxcor + b )
                        or pxcor = round ( n * pycor + a )
                       )
                 ]) [ patches in-radius-nowrap brush-radius ]
    ]
    paint-efx mypatches
end  ; line-paint

to circle-paint-edge [ cx cy ex ey ]
    locals [ inner-radius outer-radius]
    
    set-brush-radius
    ask patch cx cy
    [ set inner-radius ( round distancexy-nowrap ex ey ) - brush-radius 
      set outer-radius inner-radius + brush-width 
      paint-efx patches in-radius outer-radius with [ (distance-nowrap myself) >= inner-radius ]
     ]
end ; circle-paint-edge

to box-paint [ x1 y1 x2 y2 ]
    locals [ tempxy ]
    if x1 > x2 [ set tempxy x1 set x1 x2 set x2 tempxy ]
    if y1 > y2 [ set tempxy y1 set y1 y2 set y2 tempxy ]
    set-brush-radius
    paint-efx patches with [     pxcor >= x1 - brush-radius
                             and pxcor <= x2 + brush-radius
                             and pycor >= y1 - brush-radius
                             and pycor <= y2 + brush-radius 
                             and not
                             (     pxcor > x1 + brush-radius 
                               and pxcor < x2 - brush-radius 
                               and pycor > y1 + brush-radius
                               and pycor < y2 - brush-radius
                             )
                           ]
end

to paint-frame [ x1 y1 x2 y2 ]
    locals [ tempxy ]
    if x1 > x2 [ set tempxy x1 set x1 x2 set x2 tempxy ]
    if y1 > y2 [ set tempxy y1 set y1 y2 set y2 tempxy ]
    paint-efx patches with [     pxcor >= x1 and pxcor <= x2 
                             and pycor >= y1 and pycor <= y2
                           ]
end

to circle-paint-solid [ cx cy ex ey ]
    locals [ mypatches myradius]
    
    set-brush-radius
    ask patch cx cy
    [ set myradius distancexy-no-wrap ex ey ]
    set mypatches patches with [ distancexy-nowrap cx cy <= myradius ]
    paint-efx mypatches
end ; circle-paint-solid

to-report efx-result
; effects: "1 solid" "2 dappled" "3 undapple" "4 darken" "5 lighten" "6 blend"
    locals [ efx ]
    set efx item 0 effect
    ifelse efx = "1" [ report brush-color ]
  [ ifelse efx = "2" [ report dappled ]
  [ ifelse efx = "3" [ report center? pcolor ]
  [ ifelse efx = "4" [ report undapple? pcolor ]
  [ ifelse efx = "5" [ report darker? pcolor ]
  [ ifelse efx = "6" [ report lighter? pcolor ]
  [ ifelse efx = "7" [ report blend pcolor ]
                   [ stop ]
  ]]]]] ]
end ; efx-result
    
to paint-efx [ mypatches ]
; effects: "1 solid" "2 dappled" "3 undapple" "4 darken" "5 lighten" "6 blend"
    locals [ efx ]
    set efx item 0 effect
    ifelse efx = "1" [ ask mypatches [ set pcolor brush-color ] ]
  [ ifelse efx = "2" [ ask mypatches [ set pcolor dappled ] ]
  [ ifelse efx = "3" [ ask mypatches [ set pcolor center? pcolor ] ]
  [ ifelse efx = "4" [ ask mypatches [ set pcolor undapple? pcolor ] ]
  [ ifelse efx = "5" [ ask mypatches [ set pcolor darker? pcolor ] ]
  [ ifelse efx = "6" [ ask mypatches [ set pcolor lighter? pcolor ] ]
  [ ifelse efx = "7" [ ask mypatches [ set pcolor blend pcolor ] ]
                   [ stop ]
  ]]]]] ]
end ; paint-efx

to stamp-efx      
; effects: "1 solid" "2 dappled" "3 undapple" "4 darken" "5 lighten" "6 blend"
  set pcolor efx-result
  set unpainted? false
end ; stamp-efx
      

to fill-solids [ old-color ]
    locals
    [ fillable
      my-pxcor
      my-pycor
    ]

    if pcolor = old-color and unpainted? != false
    [ stamp-efx
      set my-pxcor pxcor
      set my-pycor pycor
      set fillable neighbors4 with [     pcolor = old-color 
                                     and unpainted? != false
                                     and abs (pxcor - my-pxcor) < 2
                                     and abs (pycor - my-pycor) < 2
                                   ]
      if any? fillable [ ask fillable [ fill-solids old-color ] ]
    ]    
end ; fill-solids

to fill-shades [ old-color ]
   locals
   [ new-color
     fillable
     my-pxcor
     my-pycor
   ]

   set new-color efx-result
   if shade-of? pcolor old-color
      and unpainted?
      and not shade-of? pcolor new-color
   [ set pcolor new-color
     set my-pxcor pxcor
     set my-pycor pycor
     set fillable neighbors4 with [     shade-of? old-color pcolor
                                    and unpainted?
                                    and distance-nowrap myself < 2
                                    ;and abs (pxcor - my-pxcor) < 2
                                    ;and abs (pycor - my-pycor) < 2 
                                  ]
     if any? fillable [ ask fillable [ fill-shades old-color ] ]
   ]
end ; fill-shades

;;
;;  COLOR PROCESSING REPORTERS
;;

to-report base-color
    ; reports base-color of brush
    ; e.g. if brush is red, base-color is red - 5.
    ; if brush is 86.875, base-color is 80.000
    report 10 * int ( brush-color * .1 )
end

to-report base-color? [ hue ] ; reports base-color of any hue
    report 10 * int ( hue * .1 )
end

to-report tint
    ; reports tint of brush
    ; e.g. if brush is red, tint is 5
    ; if brush is 86.8753, tint is 6.8753
    report precision ( brush-color - base-color ) 4
end

to-report tint? [ hue ] ; reports tint of any hue
    report precision (hue - base-color? hue) 4
end

to-report  get-rgb [ rgb-item hue ]
   ; reports the r, g, or b (0 1 or 2) of the hue
   report item rgb-item (extract-rgb hue)
end

to-report inverse? [ hue ]
    report rgb (1 - get-rgb 0 hue ) (1 - get-rgb 1 hue) (1 - get-rgb 2 hue)
end

to-report blend [ hue2 ]
    locals [ rr1 gg1 bb1 rr2 gg2 bb2 ]

    set rr1 get-rgb 0 brush-color  * strength
    set gg1 get-rgb 1 brush-color  * strength
    set bb1 get-rgb 2 brush-color  * strength
    
    set rr2 get-rgb 0 hue2  * ( 1 - strength )
    set gg2 get-rgb 1 hue2  * ( 1 - strength )
    set bb2 get-rgb 2 hue2  * ( 1 - strength )

    report rgb ( ( rr1 + rr2 ) )
               ( ( gg1 + gg2 ) )
               ( ( bb1 + bb2 ) )
end

to-report blend? [ hue1 hue2 ]
    locals [ rr1 gg1 bb1 rr2 gg2 bb2 ]

    set rr1 ( get-rgb 0 hue1 ) * strength
    set gg1 ( get-rgb 1 hue1 ) * strength
    set bb1 ( get-rgb 2 hue1 ) * strength
    
    set rr2 ( get-rgb 0 hue2 ) * ( 1 - strength )
    set gg2 ( get-rgb 1 hue2 ) * ( 1 - strength )
    set bb2 ( get-rgb 2 hue2 ) * ( 1 - strength )

    report rgb ( ( rr1 + rr2 ) )
               ( ( gg1 + gg2 ) )
               ( ( bb1 + bb2 ) )
end

to-report dappled ; dapples the brush; reports a random tint of the brush color
    report precision ( base-color +  5 - 5.0 * strength + (10.0 * random-int-or-float strength) ) 4
end

to-report dappled? [ hue ] ; dapples any color
    report precision ( base-color? hue +  5 - 5.0 * strength + (10.0 * random-int-or-float strength) ) 4
end


to-report darker
    locals [ new-tint ]
    set new-tint precision ( tint - .5 ) 1
    if new-tint < 0 
    [ set new-tint 0 ]
    report precision ( base-color + new-tint ) 4
end

to-report lighter
    locals [ new-tint ]
    set new-tint precision ( tint + .5 ) 1
    if new-tint > 9.9999 
    [ set new-tint 9.9999 ]
    report precision ( base-color + new-tint ) 4
end

to-report darker? [ hue ]
    locals [ new-tint ]
    set new-tint precision ( ( tint? hue ) - .5 ) 1 
    if new-tint < 0 
    [ set new-tint 0 ]
    report precision ( base-color? hue + new-tint ) 4
end

to-report lighter? [ hue ]
    locals [ new-tint ]
    set new-tint  precision ( ( tint? hue )  + .5 ) 1
    if new-tint > 9.9999 
    [ set new-tint 9.9999 ]
    report precision ( base-color? hue + new-tint ) 4
end

to-report undapple; removes shades from the brush color. i.e. color is set to multiple of 5
    report 5.0 + base-color
end

to-report undapple? [ hue ] ; removes shades from any color
    report 5.0 + base-color? hue 
end

to-report center ;; Centers the brush color
;; center is a smarter version of undapple
;; undapple always reports the true center, so black and white turn gray.
;; center will turn dark shades of gray black, and light shades white.
    locals [ my-tint my-base ]
    set my-base base-color
    ifelse my-base != 0
    [ report my-base + 5 ]
    [ set my-tint tint
        ifelse my-tint < 3.3333 [ report black ]
      [ ifelse my-tint < 6.6666 [ report gray  ]
                                 [ report white ]
      ]
    ]
end


to-report center? [ hue ] ; centers any color
;; center is a smarter version of undapple
;; undapple always reports the true center, so black and white turn gray.
;; center will turn dark shades of gray black, and light shades white.
    locals [ my-tint my-base ]
    set my-base base-color? hue
    ifelse my-base != 0
    [ report my-base + 5 ]
    [ set my-tint tint? hue
        ifelse my-tint < 3.3333 [ report black ]
      [ ifelse my-tint < 6.6666 [ report gray  ]
                                [ report white ]
      ]
    ]
end

to-report un-center
; un-center is a smarter version of dapple
; dapple turns black, white, and gray to random shades of gray,
; so that dapple is not reversable by center, since dapple could turn black
; into lightest gray.  un-center, when applied to:
; black, produces dark shades of gray
; gray, produces medium shades of gray
; white, produces light shades of gray
    ifelse brush-color >= 10
    [ ; for non-b/g/w colors, use dappled
      report dappled
    ]
    [ ifelse brush-color > 6.6666
      [ report 6.666 + random-float 3.3333 ]
      [ ifelse brush-color > 3.3333
        [ report 3.3333 + random-float 3.3333 ]
        [ report random-float 3.3333 ]
      ]
    ]
end

to-report un-center? [ hue ]
; un-centers the given color
    ifelse hue >= 10
    [ report dappled? hue ]
    [ ifelse hue < 3.3333
      [ report random-int-or-float 3.3333 ]
      [ ifelse hue < 6.6666
        [ report 3.3333 + random-int-or-float 3.3333 ]
        [ report 6.6666 + random-int-or-float 3.3333 ]
      ]
    ]
end

to-report white-out
    report precision ( 9.9999 + base-color ) 4
end

;;
;; COLOR-NAME
;;

to-report color-name [ hue ] ; returns a string naming the color
    locals
    [ name
      my-tint
      tint-name
      base
    ]
    set base center? hue
    set my-tint tint? hue
    set name ""
    set tint-name ""
    if base = black     [ set name "black" set my-tint 10 - my-tint ]
    if base = white     [ set name "white" set my-tint 10 - my-tint ]
    if base = blue      [ set name "blue" ]
    if base = brown     [ set name "brown" ]
    if base = cyan      [ set name "cyan" ]
    if base = gray      [ set name "gray" ]
    if base = green     [ set name "green" ]
    if base = lime      [ set name "lime" ]
    if base = magenta   [ set name "magenta" ]
    if base = orange    [ set name "orange" ]
    if base = pink      [ set name "pink" ]
    if base = red       [ set name "red" ]
    if base = sky       [ set name "sky" ]
    if base = turquoise [ set name "turquoise" ]
    if base = violet    [ set name "violet" ]
    if base = yellow    [ set name "yellow" ]
    if name = ""        [ set name "unknown" ]
      ifelse my-tint <= 0.0000  [ set tint-name "darkest "  ]
    [ ifelse my-tint <= 2.5     [ set tint-name "darker "   ]
    [ ifelse my-tint <  5.0     [ set tint-name "dark "     ]
    [ ifelse my-tint =  5.0     [ set tint-name "pure "     ]
    [ ifelse my-tint <  7.5     [ set tint-name "light "    ]
    [ ifelse my-tint <  9.9999  [ set tint-name "lighter "  ]
    [ ifelse my-tint >= 9.9999  [ set tint-name "lightest " ]
      []
    ]]]]]]
    report (tint-name + name)
end; color-name

;;
;; STATUS DISPLAY
;;

to-report status-display
    locals [ point-color result pointer-ok?]
    set result ( word tool ":" )
    set pointer-ok? is-turtle? pointer and pointer != nobody
    ifelse tool = "pick-color" and pointer-ok?
    [ set point-color color-of pointer
      ifelse point-color = brush-color
      [ set result ( word result "(curr color) " ) ]
      [ set result ( word result "set brush to " ) ]
      set result ( word result point-color " (" ( color-name point-color ) ")" )
    ] [
    ifelse tool = "change-color" and pointer-ok?
    [ set point-color color-of pointer
      ifelse point-color = brush-color
      [ set result ( word result "(curr color) " ) ]
      [ set result ( word result point-color " (" ( color-name point-color ) ") to " ) ]
      set result ( word result brush-color "(" ( color-name brush-color ) ")" )
    ] [
    set result ( word result brush-color ", " ( color-name brush-color ) "(" effect ") " )
    if pointer-ok? 
    [ set result ( word result "(" ( xcor-of pointer ) "," ( ycor-of pointer ) ")" ) ]
    ] ]
    report result
end ; monitor-color

;;
;;
;; MATH HELPER REPORTERS
;;

to-report sign [ value ]
    ; reports the sign of a number, as 1, 0, or -1
    ifelse value = 0
    [ report 0 ]
    [ ifelse value < 1
      [ report -1 ]
      [ report  1 ]
    ]
end ; sign

to-report plus-or-minus-one
    ; randomly reports +1 or -1
    report random 2 * 2 - 1
end

to-report zero-or-one
    ; randomly reports 0 or 1
    report random 2
end

;;
;; CUSTOM TOOLS PROCEDURES
;;

to shade-patches
    ask patches
    [ set pcolor shade-edge ]
    history-record
end

to-report shade-edge
    locals [ color-mates my-color mates-count max-mates]
    set my-color base-color? pcolor
    set color-mates patches in-radius-nowrap cycles with [ self != myself ]
    set max-mates 1 + count color-mates
    set color-mates color-mates with [ base-color? pcolor = my-color ]
    set mates-count count color-mates
    if mates-count > max-mates [ set mates-count max-mates ]
    report (my-color + 10 * (mates-count / max-mates ))
end

to blur-patches
   ; caluclate the blur color for all patches
   ask patches
   [ set sample-color blur ]
   ; apply the blur color
   ask patches
   [ set pcolor sample-color ]
    history-record
end

to-report blur
      ; reports the pcolor of the calling agent blurred with the pcolor of its neighbors
      locals [ rmean gmean bmean ]
      set rmean (( mean values-from neighbors [ get-rgb 0 pcolor ] ) * strength + ( ( get-rgb 0 pcolor ) * (1 - strength) ))
      set gmean (( mean values-from neighbors [ get-rgb 1 pcolor ] ) * strength + ( ( get-rgb 1 pcolor ) * (1 - strength) ))
      set bmean (( mean values-from neighbors [ get-rgb 2 pcolor ] ) * strength + ( ( get-rgb 2 pcolor ) * (1 - strength) ))
      report rgb rmean gmean bmean
end

;;
;; IMAGE SHIFT, FLIP and ROTATE
;;

to image-shift-all [ sdx sdy amount ]
    set sdx sdx * amount
    set sdy sdy * amount 
    ask patches
    [ set sample-color pcolor-of patch-at sdx sdy ]
    ask patches
    [ set pcolor sample-color ]
    history-record
end

to image-flip-xy [ direction ]
    ask patches
    [ set sample-color pcolor-of patch ( pxcor * direction ) ( pycor * (- direction) )  ]
    ask patches
    [ set pcolor sample-color ]
    history-record
end

to image-rotate-90
      ask patches
      [ ifelse abs pxcor <= screen-edge-y and abs pycor <= screen-edge-x 
        [ set sample-color pcolor-of patch (0 - pycor) (pxcor) ]
        [ set sample-color canvas ]
      ]
      ask patches
      [ set pcolor sample-color ]
      history-record
end

;;
;; HISTORY UNDO / REDO PROCEDURES
;;

to history-trim
    ; removes undo information above the current undo point.
    repeat undo-point 
    [ set history but-first history ]
end    

to history-record
    ; history is recorded AFTER each change, so last item in history is CURRENT appearance
    if undo-on? 
    [ ifelse not is-list? history-of patch 0 0
      [ history-reset ]
      [ history-add-item ]
    ]
end ; record-history

to history-add-item
    ; if any redos after this point, delete them
    if undo-point > 0
    [ ask patches with [ true ]
      [ history-trim ]
      set undo-length length history-of patch 0 0
      set undo-point 0
    ]
    ; record this point in the history
    ask patches with [ true ]
    [ set history fput pcolor history ]
    set undo-length undo-length + 1
    ; if history has exceeded depth, trim oldest item.
    while [ undo-length > undo-levels ]
    [ ask patches with [ true ]
      [ set history but-last history ]
      set undo-length undo-length - 1
    ]
   
end ; add-history-item

to history-undo
    ; history is recorded AFTER each change, so list item in history is CURRENT appearance
    ; is there any history?
    if undo-length > 1 and undo-point < undo-length - 1
      [ ; there is at least one history item before the current appearance
        ; move back in the history
        ; the current appearance is the head of the list (item 0 )
        ; the undo point counts forward into the history.
        ; if we have not undo'd all the way back yet...
        ; apply colors from history
        ; zero is current appearance, 1 is previous appearance...
        set undo-point undo-point + 1
        ask patches
        [ set pcolor item undo-point history ]
      ] ; if undo-length...
end ; undo

to history-redo
      if undo-point > 0
      [ ; we are in the history, so lets move up to the previous entry
        set undo-point undo-point - 1
        ; apply colors from history
        ask patches 
        [ set pcolor item undo-point history ]
      ]
end ; redo

to history-reset
    if undo-on?
    [ ask patches
      [ set history [] ]
      set undo-point 0
      set undo-length 0
      history-add-item
    ]
end

;;
;; FILE I/O PROCEDURES
;;

;;
;; I/O HELPERS
;;

to-report file-ends-with-ext? [ filename ext ]
   ; reports true if the string filename ends with the characters in ext
   report substring filename (length filename - length ext ) ( length ext ) != ext
end

to-report file-without-ext [ filename ]
   locals [ pre-dot ]
   set pre-dot position "." ( reverse filename )
   ifelse pre-dot = false
   [ report filename ]
   [ set pre-dot ( length filename ) - pre-dot - 1
     report substring filename 0 pre-dot
   ]
end

to file-open-overwrite [ filename ]
   file-open filename
   file-close
   if file-exists? filename
   [ ; if file exists, delete
     ; to ensure creating a new file
     file-delete filename
   ]
   file-open filename
end
     
;;
;; FILE I/O USER INTERFACE WRAPPERS
;;
 
to file-save-patches [ filename ] 
    ; saves the patches using the given filename
    ; if the filename is blank
    ; prompts for the filename
    ; so, save... is  save ""
    ; and save!   is  save current-file
    ;
 
    ; if filename is blank, or not a string, prompt for file-name
    if filename = "" or not is-string? filename
    [ set filename user-choose-new-file ]
    if filename != false
    [ ; filename is in order, call the procedure to write the file
      file-write-nim-2 filename
      set current-file filename
    ]
end 

to file-load-patches [ filename ] 
    ; loads the patches using the given file-name
    ; if the file-name is blank
    ; prompts for the filename
    ; so, load... is  load ""
    ; and load!   is  load current-file
    ;
 
    ; if filename is blank, or not a string, prompt for file-name
    if not is-string? filename or filename = ""
    [ set filename user-choose-file ]
    if is-string? filename and filename != ""
    [ ;; file name is in order, call the procedure to read the file
      no-display
      file-read-nim-2 filename
      set current-file filename
      display
    ]
    history-record
end    

to file-save-patches-csv [ filename ] 
    ; saves the patches using the given filename
    ; if the filename is blank
    ; prompts for the filename
    ; so, save... is  save ""
    ; and save!   is  save current-file
    ;
 
    ; if filename is blank, or not a string, prompt for file-name
    if filename = "" or not is-string? filename
    [ set filename user-choose-new-file ]
    if is-string? filename and filename != ""
    [ ; filename is in order, call the procedure to write the file
      file-write-csv filename
      ; set current-file filename
    ]
end 

to file-load-patches-csv [ filename ] 
    ; loads the patches using the given file-name
    ; if the file-name is blank
    ; prompts for the filename
    ; so, load... is  load ""
    ; and load!   is  load current-file
    ;
 
    ; if filename is blank, or not a string, prompt for file-name
    if filename = "" or not is-string? filename
    [ set filename user-choose-file ]
    if is-string? filename and filename != ""
    [ ;; file name is in order, call the procedure to read the file
      file-read-csv filename
      ; set current-file filename
    ]
    history-record
end    

;;
;; FILE I/O READ AND WRITE PROCEDURES
;;
        
to file-read-nim-2 [ filename ]
    ; reads a .nim file in the nim 2.0 format
    locals [ format-code size-x size-y 
              sw-x sw-y  ; coordinates of south-west corner
              x-in y-in c-in
           ]
    set io? true       
    if not is-string? filename or filename = ""
    [ stop ]
    file-close-all
    file-open filename
    set format-code file-read
    if format-code != "nim 2.0"
    [ stop ]
    set size-x file-read
    set size-y file-read
    set x-in (- size-x)
    set y-in (- size-y)
    while [ not file-at-end? ]
    [ set c-in file-read
      if abs x-in <= screen-edge-x and abs y-in <= screen-edge-y 
      [ set pcolor-of ( patch x-in y-in ) c-in ]
      set y-in y-in + 1
      if y-in > size-y
      [ display
        no-display
        set y-in (- size-y)
        set x-in x-in + 1
      ]
    ]
    file-close
    set io? false
end

to file-write-nim-2 [ filename ]
    ; writes a .nim file in the nim 2.0 format
    locals [ format-code size-x size-y 
              sw-x sw-y  ; coordinates of south-west corner
              x-out y-out c-out
           ]
    set io? true           
    if not is-string? filename or filename = ""
    [ stop ]
    ; open new file
    file-open-overwrite filename
    ; write format header
    file-write "nim 2.0"
    ; write screen dimensions
    file-write screen-edge-x
    file-write screen-edge-y
    ; write patch data
    set x-out (- screen-edge-x)
    repeat screen-size-x
    [ set y-out (- screen-edge-y)
      repeat screen-size-y
      [ file-write pcolor-of patch x-out y-out
        set y-out y-out + 1
      ]
      set x-out x-out + 1
    ]
    file-close
    set io? false
end

to file-write-csv [ filename ]
    ; writes a truncated netlogo csv file, with no extra variables
    ; which can be imported into any model using import-world
    locals [ q      ; quote character
             qcq    ; quote comma quote ( "," ) seperator
             qcc    ; the final quote and 2 commas: placeholders for plabel and plabel-color
             x-out
             y-out
             c-out
             
           ]
    set io? true           
    if not is-string? filename or filename = ""
    [ stop ]
    ; initialize helper constants
    set q "\""
    set qcq (word q "," q )
    set qcc (word  q ",," )
    ; open file at beginning
    file-open-overwrite filename
    ; write headers
    file-print ( word q "drawing-tool-2004 patch-data (NetLogo 2.0.0)" q )
    file-print ( word q "drawing-tool-2004.nlogo" q )
    file-print ( word q get-date-and-time q )
    file-print ""
    ; write global variables
    file-print ( word q "GLOBALS" q )
    file-print ( word q "screen-edge-x" qcq "screen-edge-y" q )
    file-print ( word q screen-edge-x qcq screen-edge-y q )
    file-print ""
    ; write turtle data (none)
    file-print ( word q "TURTLES" q )
    file-print ( word q "who" qcq "color" qcq "heading" qcq "xcor" qcq "ycor" qcq "shape" qcq
                        "pen-down?" qcq "label" qcq "label-color" qcq "breed" qcq "hidden?" qcq "size" q
               )
    file-print ""
    ; write patch data
    file-print ( word q "PATCHES" q )
    file-print ( word q "pxcor" qcq "pycor" qcq "pcolor" qcq "plabel" qcq "plabel-color" q )
    set y-out screen-edge-y
    repeat screen-size-y
    [ set x-out (- screen-edge-x)
      repeat screen-size-x
      [ file-print ( word q x-out qcq y-out qcq (pcolor-of patch x-out y-out) qcc )
        set x-out x-out + 1
      ]
      set y-out y-out - 1
    ]
    ; close file
    file-close
    set io? false
end

to file-read-csv [ filename ]
    ; reads a .csv netlogo world file 
    locals [ format-code size-x size-y 
              sw-x sw-y  ; coordinates of south-west corner
              x-in y-in c-in
           ]
    set io? true       
    if not is-string? filename or filename = ""
    [ stop ]
    set io? false
    import-world filename
end

to file-save-png [ filename ]
    if filename = "" or not is-string? filename
    [ set filename user-choose-new-file ]
    if filename != false
    [ export-graphics filename ]
end


@#$#@#$#@
GRAPHICS-WINDOW
466
10
731
296
25
25
5.0
1
10
1
1
1

CC-WINDOW
364
540
499
599
Command Center

BUTTON
13
512
180
545
clear-canvas
    ask patches\n    [ set pcolor canvas\n      set unpainted? true\n    ]\n    history-record
NIL
1
T
OBSERVER
NIL

BUTTON
10
10
122
43
drawing tool ON
main-drawing-tool-loop
T
1
T
OBSERVER
NIL

SLIDER
12
442
180
475
brush-color
brush-color
0
139.9999
45.0
1.0E-4
1
NIL

BUTTON
11
197
66
230
red
set brush-color red
NIL
1
T
OBSERVER
T

BUTTON
11
232
66
265
orange
set brush-color orange
NIL
1
T
OBSERVER
T

BUTTON
11
302
66
335
yellow
set brush-color yellow
NIL
1
T
OBSERVER
T

SLIDER
12
477
123
510
canvas
canvas
0
139.9999
9.9999
1.0E-4
1
NIL

BUTTON
68
197
123
230
green
set brush-color green
NIL
1
T
OBSERVER
T

BUTTON
68
267
123
300
turquoise
set brush-color turquoise
NIL
1
T
OBSERVER
T

BUTTON
68
302
123
335
cyan
set brush-color cyan
NIL
1
T
OBSERVER
T

BUTTON
125
197
180
230
blue
set brush-color blue
NIL
1
T
OBSERVER
T

BUTTON
68
337
123
370
sky
set brush-color sky
NIL
1
T
OBSERVER
T

BUTTON
125
232
180
265
violet
set brush-color violet
NIL
1
T
OBSERVER
T

BUTTON
125
267
180
300
mgnta
set brush-color magenta
NIL
1
T
OBSERVER
T

BUTTON
11
407
66
440
_0.0000
set brush-color base-color
NIL
1
T
OBSERVER
T

BUTTON
125
407
180
440
_9.9999
set brush-color white-out
NIL
1
T
OBSERVER
T

BUTTON
125
477
180
510
<=brush
set canvas brush-color
NIL
1
T
OBSERVER
NIL

BUTTON
11
162
66
195
black
set brush-color black
NIL
1
T
OBSERVER
T

BUTTON
68
162
123
195
gray
set brush-color gray
NIL
1
T
OBSERVER
T

BUTTON
125
162
180
195
white
set brush-color white
NIL
1
T
OBSERVER
T

BUTTON
11
267
66
300
brown
set brush-color brown
NIL
1
T
OBSERVER
T

BUTTON
68
232
123
265
lime
set brush-color lime
NIL
1
T
OBSERVER
T

BUTTON
11
372
66
405
darker
set brush-color darker
NIL
1
T
OBSERVER
T

BUTTON
125
372
180
405
lighter
set brush-color lighter
NIL
1
T
OBSERVER
T

BUTTON
68
372
123
440
pure
set brush-color center
NIL
1
T
OBSERVER
T

BUTTON
125
302
180
335
pink
set brush-color pink
NIL
1
T
OBSERVER
T

SLIDER
239
97
352
130
brush-width
brush-width
1
25
1
2
1
NIL

CHOICE
10
96
123
141
tool
tool
"brush" "lines" "frames" "boxes" "rings" "circles" "fills" "fill-shades" "change-color" "pick-color"
7

BUTTON
185
563
240
596
posterize
ask patches\n[ set pcolor center? pcolor ]\nhistory-record
NIL
1
T
OBSERVER
T

BUTTON
13
563
68
596
dapple-all
ask patches\n[ set pcolor un-center? pcolor ]\nhistory-record
NIL
1
T
OBSERVER
T

BUTTON
70
563
125
596
diffuse
repeat cycles\n[ diffuse pcolor strength ]\nask patches\n[ set pcolor precision pcolor 4 ]\nhistory-record
NIL
1
T
OBSERVER
T

SLIDER
198
249
290
282
strength
strength
0
1
0.18
0.01
1
NIL

SLIDER
292
249
384
282
cycles
cycles
0
100
1
1
1
NIL

MONITOR
10
45
464
94
status bar
status-display
0
1

CHOICE
125
96
237
141
effect
effect
"1 solid" "2 dappled" "3 center" "4 undapple" "5 darken" "6 lighten" "7 blend"
1

BUTTON
331
478
407
511
clear-cursors
no-display\nclear-turtles\ndisplay
NIL
1
T
OBSERVER
T

BUTTON
242
563
316
596
shade-edges
shade-patches\nhistory-record
NIL
1
T
OBSERVER
T

BUTTON
201
443
256
476
save-nim
file-save-patches ""
NIL
1
T
OBSERVER
NIL

BUTTON
201
478
256
511
load-nim
file-load-patches ""
NIL
1
T
OBSERVER
NIL

BUTTON
315
302
370
335
//
image-shift-all -1 -1 shift
NIL
1
T
OBSERVER
T

BUTTON
333
337
388
370
>>
image-shift-all -1 0 shift
NIL
1
T
OBSERVER
T

BUTTON
258
372
313
405
\\//
image-shift-all 0 1 shift
NIL
1
T
OBSERVER
T

BUTTON
182
337
237
370
<<
image-shift-all 1 0 shift
NIL
1
T
OBSERVER
T

BUTTON
258
302
313
335
//\\
image-shift-all 0 -1 shift
NIL
1
T
OBSERVER
T

BUTTON
201
372
256
405
//
image-shift-all 1 1 shift
NIL
1
T
OBSERVER
T

BUTTON
315
372
370
405
\\
image-shift-all -1 1 shift
NIL
1
T
OBSERVER
T

BUTTON
198
302
256
335
\\
image-shift-all 1 -1 shift
NIL
1
T
OBSERVER
T

SLIDER
239
337
331
370
shift
shift
1
20
20
1
1
NIL

BUTTON
390
302
445
335
rotate
image-rotate-90
NIL
1
T
OBSERVER
T

BUTTON
390
337
445
370
flip <-->
image-flip-xy -1
NIL
1
T
OBSERVER
T

BUTTON
390
372
445
405
flip /\ \/
image-flip-xy 1
NIL
1
T
OBSERVER
T

SWITCH
198
197
310
230
undo-on?
undo-on?
0
1
-1000

SLIDER
312
197
424
230
undo-levels
undo-levels
1
100
48
1
1
NIL

BUTTON
198
162
253
195
undo
history-undo
NIL
1
T
OBSERVER
T

BUTTON
312
162
367
195
redo
history-redo
NIL
1
T
OBSERVER
T

BUTTON
369
162
430
195
clear-undo
history-reset
NIL
1
T
OBSERVER
NIL

BUTTON
255
162
310
195
undo 5
repeat 5 [ history-undo ]
NIL
1
T
OBSERVER
T

TEXTBOX
11
143
178
161
Brush Color Selection

TEXTBOX
201
284
353
302
Image Shift and Rotate

TEXTBOX
198
143
288
161
Undo and Redo

TEXTBOX
13
545
103
563
Custom Tools

TEXTBOX
198
230
349
248
Blend/Diffuse Controls

BUTTON
331
443
386
476
save-png
file-save-png ""
NIL
1
T
OBSERVER
NIL

BUTTON
249
513
343
546
default-directory?
set-current-directory user-choose-directory
NIL
1
T
OBSERVER
NIL

BUTTON
400
10
463
43
NIL
setup
NIL
1
T
OBSERVER
NIL

TEXTBOX
201
425
291
443
File Save, Load

SWITCH
356
97
464
130
markers?
markers?
0
1
-1000

BUTTON
128
563
183
596
blur
blur-patches\nhistory-record
NIL
1
T
OBSERVER
NIL

BUTTON
265
443
320
476
save-csv
file-save-patches-csv ""
NIL
1
T
OBSERVER
NIL

BUTTON
265
478
320
511
load-csv
file-load-patches-csv ""
NIL
1
T
OBSERVER
T

@#$#@#$#@
WHAT IS IT?
-----------
A drawing tool for NetLogo, implemented in NetLogo 2.0.
The usual tools are here: brush, line, circle, box, fill, plus a multi-level undo / redo feature.
Patch colors can be saved and loaded in a simple text format called .NIM, or in specially non-disruptive netlogo world files in .CSV format. PNG files can be saved as well.

Drawing-Tool can be used to draw initial patch color arrangements for experiments, or just for fun! It was designed for those times when using a program or algorithm to draw the initial arrangement isn't practical, or it would be easier to just draw the configuration.

HOW IT WORKS
------------
The "setup" routine is run automatically.
Click "Drawing Tool ON" to begin.
Choose a tool and effect from the pick-lists.
Choose a brush width and brush-color.
Click in the patch area to draw.

BRUSH-WIDTH
---------------
Use to set the brush-width used by the tools. Is always odd, as the brush is always centered on a patch.

BRUSH-COLOR
-----------
Sets the color of the brush, in increments of .0001

STRENGTH - CYCLES
-----------------
strength: controls the "strength" of various effects.
          blend: stronger means less transparent
          dapple: stronger means a wider range of shades
          diffuse: stronger diffuses more
          --use for any control or effect that requires a float value from 0.0 to 1.0
cycle:    diffuse:    repeats the diffuse function that many times
          --use for any control of effect that requires an integer value

THE COLOR PALETTE
-----------------
buttons:  Sets the brush to the named color.
lighter,
darker:   Affect the brush color (by even 0.5 increments)
pure:     Sets the brush color to the "pure" color, aka the center color.
          Example: If brush is 19.455 (light red),
                 changes it to 15.000 (red)
          Note: Pure uses the Center function, so darker grays become black,
          brighter grays become white, and middle grays become gray.
_0.0000:  Sets the brush to the darkest shade of the current color.
          Example: If brush is 117.500 (lighter violet),
                  changes color to 110.000 (darkest violet).
_9.9999:  Sets the brush to the lightest shade of the current color

canvas:   Sets the canvas color
<=brush:  Makes the current brush-color the canvas color, but does not clear the canvas
clear-
 canvas:  Wipes the canvas with the current canvas color

THE TOOLS
---------
Tools alter the canvas in different ways, using the current brush color and / or effect. (Except for pick-color, which just changes the brush color)
Drawing tools use the press-drag-release method:
  Press and hold the mouse button to start, drag to the end, release.
Note that dragging outside the drawing frame border "releases" the mouse button!

brush:        click and drag to paint
lines:        draws a line with rounded endpoints using the brush-width
frames:       draws a rectangular outline as thick as the brush width
boxes:        draws a filled rectangle, brush width is not used
fill:         fills the selected solid-color region
fill-shades:  like fill, but fills through shades of the selected color.
circles:      draws a filled circle
rings:        draws a ring, using brush thickness
pick-color:   click on the canvas to change the brush color to the
              color of the selected patch
change-color: click on the canvas to change every patch with that color to the brush color


THE EFFECTS
------------
solid:    draws in the selected color
dappled:  draws in random shades of the selected color
          change the strength slider to adjust the range of shades
center:   changes the current patch color to the pure shade of that color,
          Grays are special. Darker gray becomes black and lighter gray becomes white
undapple: changes the current patch color to the pure shade of that color
darken:   changes the current patch color to be slightly darker
lighten:  changes the current patch color to be slightly lighter
blend:    draws in the slected color, blended with the current patch color.
          set the opacity with the strength slider.

SAVING AND LOADING
------------------
Save-nim: Save the patch colors, using nim 2.0 format
Load-nim: Load the patch colors, expecting nim 2.0 format
Save-csv: Save the patch colors and screen-dimentions,
              using netlogo 2.0  export-world cvs format.
Load-csv: Load the patch colors and set the screen-dimentions,
              using netlogo 2.0 export-world cvs format.

Save-Png: Save the graphics window as a png file
Clear-cursors: Removes the paint-tool cursor, so it does not appear in the png image.

Set-Current-Directory: Lets you set the current directory for subsequent prompts
   This will NOT change the directory of the current file!

Difference between NIM and CSV
------------------------------
drawing-tool-2004 can write and read two formats, nim and csv. It can also write PNG files using the built-in export-graphics primitive.

NIM is a custom format that contains the image dimensions and the patch colors.
CSV is a dumbed-down netlogo export-world file, that contains patch coordinates and colors.

The main difference is that loading a csv file will change the world dimensions to match the dimensions saved in the file, just like import-world, and csv load clears the undo history.

The advantage of CSV format is that any netlogo model can load the data using import-world, with no additional coding.

The advantage of NIM is that is lets one overlay smaller images, or crop larger images. It also makes slightly smaller files.

ADDITIONAL TOOLS / EFFECTS / CONTROLS
-------------------------------------
setup:         resets the drawing cursor and clears the canvas

SHIFT, FLIP, and ROTATE
~~~~~~~~~~~~~~~~~~~~~~~
shifters:      shifts the patch colors in the indicated direction, by the amount of the shift slider
flip <-->:     Flips the world along the center vertical axis
flip /\\/:     Flips the world along the center horizontal axis
rotate:        rotates the patches 90 degrees.
               Caution: if the world is not square, color data in patches
               outside the center square will be lost

UNDO
~~~~
undo-on?:     enables or disables the undo feature
undo:         removes the last change.
redo:         reapplies the changes removed by undo.
clear-undo:   deletes the undo-history
undo-levels:  sets the maximum number of changes that can be recorded.
              each change after than will cause the oldest change
              to be forgotten

SPECIAL EFFECT BUTTONS
~~~~~~~~~~~~~~~~~~~~~~

You can make your own special effect buttons. These are nothing more than buttons that call netlogo procedures that do various graphics things.
You could insert you model's patch setup code, and then test it with a button here.

dapple-all:   sets all patches to a random shade of their current color
              breadth of shades determined by strength
              0.0 = use pure color
              0.5 = use mostly middle shades
              1.0 = use all shades
posterize:    sets all patches to their pure color.
shade-edges:  sets color brightness based on number
              of neighbors with same base-color
diffuse:      performs diffusion at the specified strength
              for the specified number of cycles
blur:         blurs the patch colors together

SPECIAL PROGRAMMING NOTES
-------------------------
To interactively show the user the line, circle or box that will be drawn, "marker" turtles are created and destroyed. The number of marker turtles varies, depending on the screen-size. The markers can be boxes or lines.

To reduce the number of global variables, the model makes use of turtle variables and turtle inheritance when making the markers.

The flood fill functions use recursion to fill the patches.

The model makes use of a turtle's ability to directly access the variables of the patch the turtle is on: The pointer turtle performs most of the tasks, so it can directly refer to the patch color, etc.

The model uses several custom-designed turtle-shapes as the various drawing cursors.

Where there are two reporters with nearly the same name, like "blend" and "blend?" the unadorned reporter refers to the brush color, whereas the "?" reporter always requires a color argument. For example: "dappled" reports a random shade of the brush color, but "dappled? red" reports a random shade of red.

Many of the effects depend on the base-color and tint reporters.

Base-color gives the black version of the color (i.e. the even multiple of 10 for the color)

E.g. the base-color of red (15) is 10.

Tint could also be called "shade": it gives the amount above the base-color. The tint of a pure color is alway 5. The tint of black is 0, the tint of white is 9.9999.

Black, white and gray are strange cases. They all share a base color (0) and therefore, the pure color (base + 5) of all three is 5. So, undapple will always turn black and white to gray. However, center is a smarter undapple: It examines the shade of gray and turns darker gray to black (0.0), lighter gray to white (9.9999), and medium grays to gray (5.0).

Setting the size and shape every cycle caused the turtles to flicker. To avoid flicker of the cursors, the size and shape is tested first, and only set if it doesn't match. 

Likewise, the x and y coordinates are set only if the mouse moves to a different patch.

**********

A note on coding style: The code style is rather inconsistant, as I went through several phases of "preferred formatting" styles during developement. Also, I am too busy / lazy and too eager to get this model out there to spend any more time on code formatting. Please forgive me, and please emulate only the best coding styles shown here, not the worst!

**********

Thanks to everyone on the NetLogo users list for your support!

COPYRIGHT & LICENSE
-------------------
This work is Copyright © 2004 James P. Steiner.
This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/1.0/ or send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.

@#$#@#$#@
default
true
0
Polygon -7566196 true true 150 5 40 250 150 205 260 250

bx
true
0
Polygon -7566196 true true 45 45 45 255 255 255 255 45

bxo
false
10
Rectangle -16776961 false true 15 15 285 285

ch
true
10
Line -16776961 true 150 0 150 300
Line -16776961 true 0 150 300 150

chbx
false
10
Rectangle -16776961 false true 15 15 285 285
Line -16776961 true 15 150 285 150
Line -16776961 true 150 15 150 285

chc
false
10
Line -16776961 true 150 0 150 300
Line -16776961 true 0 150 300 150
Circle -16776961 false true 15 15 270

circum
true
1
Line -65536 true 0 150 299 150

cp
false
10
Rectangle -16777216 false false 0 0 85 85
Rectangle -1 false false 215 0 300 85
Rectangle -1 false false 0 215 85 300
Rectangle -16777216 false false 215 215 300 300
Rectangle -16776961 true true 15 15 75 75
Rectangle -16776961 true true 15 225 75 285
Rectangle -16776961 true true 225 15 285 75
Rectangle -16776961 true true 225 225 285 285
Line -16776961 true 150 0 150 300
Line -16776961 true 0 150 300 150

line
true
1
Line -65536 true 150 1 150 299

oc
false
10
Circle -16776961 false true 15 15 270

ray
true
10
Line -16776961 true 150 150 150 0

tip
false
10
Line -16776961 true 15 150 285 150
Line -16776961 true 150 15 150 285
Rectangle -16776961 true true 115 115 175 175

tip0
false
0
Rectangle -7566196 true true 100 100 200 200
Rectangle -1 false false 15 15 285 285
Rectangle -16777216 false false 30 30 270 270

@#$#@#$#@
NetLogo 2.0.0
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
