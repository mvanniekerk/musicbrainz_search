module Utils exposing (..)

{-| This module exposes some functions used for the MusicBrainz frontend

    # List sorting
    @docs groupByValue, sortByOccurrence, descending
-}


{-| Group values, keeping track off the number of occurrences

    groupByValue ["hey", "bye", "hey", "bye", "no way"] == [("bye",2),("hey",2),("no way",1)]

    Values are first sorted, to make the function run in O(N log N) time.
-}
groupByValue : List comparable -> List (comparable, Int)
groupByValue list =
    let
        sorted = List.sort list

        groupBy inp acc =
            let
                mhead = List.head acc
                tail = Maybe.withDefault [] <| List.tail acc
            in
                case mhead of
                    Nothing -> [(inp, 1)]
                    Just head ->
                        if Tuple.first head == inp then
                            Tuple.mapSecond (\x -> x+1) head :: tail
                        else
                            (inp, 1) :: acc
    in
        List.foldr groupBy [] sorted


{-| Sort values by occurrence.

    sortByOccurrence ["hey", "bye", "hey", "bye", "no way"] = ["bye", "hey", "no way"]

    Values that occur the same amount of times are sorted normally
-}
sortByOccurrence : List comparable -> List comparable
sortByOccurrence list =
    let
        sortBySecond : List (comparable, Int) -> List (comparable, Int)
        sortBySecond = List.sortWith (\a b -> descending (Tuple.second a) (Tuple.second b))
        takeFirst = List.map Tuple.first
    in
        list
        |> groupByValue
        |> sortBySecond
        |> takeFirst

{-| Sort descending instead of ascending.

    sortWith descending [1, 2, 3] == [3, 2, 1]
-}
descending : comparable -> comparable -> Order
descending a b =
    case compare a b of
        LT -> GT
        EQ -> EQ
        GT -> LT