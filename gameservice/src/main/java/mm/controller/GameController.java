package mm.controller;

import mm.storage.SessionStorage;
import mm.ticker.Ticker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import mm.service.GameService;

@Controller
@RequestMapping("game")
public class GameController {

    @Autowired
    GameService gameService;

    @RequestMapping(
            path = "create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> create(@RequestParam("playerCount") int playerCount) {
        long gameId = gameService.create(playerCount);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        return new ResponseEntity<>(gameId, headers, HttpStatus.OK);
    }

    @RequestMapping(
            path = "start",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> start(@RequestParam("gameId") long gameId) {
        Ticker ticker = new Ticker(SessionStorage.getSessionById(gameId));
        SessionStorage.putTicker(ticker, SessionStorage.getSessionById(gameId));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
