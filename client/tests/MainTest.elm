module MainTest exposing (..)

import Expect exposing (Expectation)
import Fuzz exposing (Fuzzer, int, list, string)
import Test exposing (..)
import Json.Decode
import Main exposing (..)
import Time


decodesActiveAlarm : Test
decodesActiveAlarm =
  test "Decodes a set alarm JSON" <|
    \() ->
      let
        input =
          """
          { "alarm":
            { "isActive": true
            , "hour" : 20 
            , "minute" : 4
            }
          }
          """
        decodedOutput =
          Json.Decode.decodeString alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok (Alarm True
            { hour = 20
            , minute = 4
            }
          ))

decodesUnsetAlarm : Test
decodesUnsetAlarm =
  test "Decodes an unset alarm JSON" <|
    \() ->
      let
        input = """{ "alarm": { } }"""
        decodedOutput =
          Json.Decode.decodeString alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok (Alarm False (Time 7 0)) )

decodesCheckIn : Test
decodesCheckIn =
  test "Decodes a check-in" <|
    \() ->
      let
        input =
          """
            {"checkIns": [
              { "time": 50
              , "battery": 95
              }
            ]}
          """
        decodedOutput =
          Json.Decode.decodeString checkInDecoder input
      in
        Expect.equal decodedOutput
          ( Ok [CheckIn (Time.millisToPosix 50000) 95] ) 

decodesUnsetCheckIn : Test
decodesUnsetCheckIn =
  test "Decodes an unset check-in" <|
    \() ->
      let
        input = """ {"checkIns": []} """
        decodedOutput =
          Json.Decode.decodeString checkInDecoder input
      in
        Expect.equal decodedOutput
          ( Ok [] )

lastInEmptyListIsNothing =
  describe "Last in a list"
  [
    (test "last [] = Nothing" <|
      \() -> Expect.equal (last []) (Nothing))

  , (test "last [1] = Just 1" <|
      \() -> Expect.equal (last [1]) (Just 1))

  , (test "last [1, 2] = Just 2" <|
      \() -> Expect.equal (last [1, 2]) (Just 2))

  ]
