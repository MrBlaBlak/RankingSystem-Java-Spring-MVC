package pl.coderslab.workshop.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.workshop.dao.*;
import pl.coderslab.workshop.model.*;
import pl.coderslab.workshop.repository.GamerRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import static java.lang.Math.abs;

@Controller
public class GamerController {
    private final MatchDao matchDao;
    private final TeamDao teamDao;
    private final MatchGamerDao matchGamerDao;
    private final KillsAndCapsDao killsAndCapsDao;
    private final Gamer[] team1gamers;
    private final Gamer[] team2gamers;
    private final GamerRepository gamerRepository;


    public GamerController(MatchDao matchDao, TeamDao teamDao, MatchGamerDao matchGamerDao, KillsAndCapsDao killsAndCapsDao, GamerRepository gamerRepository) {
        team1gamers = new Gamer[5];
        team2gamers = new Gamer[5];
        for (int a = 0; a < 5; a++) {
            team1gamers[a] = new Gamer();
            team2gamers[a] = new Gamer();
        }
        this.matchGamerDao = matchGamerDao;

        this.matchDao = matchDao;
        this.teamDao = teamDao;
        this.killsAndCapsDao = killsAndCapsDao;
        this.gamerRepository = gamerRepository;
    }


    @RequestMapping("/gamer/add")
    @ResponseBody
    public void addGamer() {
        Gamer gamer = new Gamer();
        gamer.setMmr(631.4);
        gamer.setName("Suddi");
        gamer.setServer("EU1");
        gamer.setLastTen("1010111110");
        gamerRepository.save(gamer);
    }

    @RequestMapping("/gamer/addGamers")
    @ResponseBody
    public void readTextFile() throws FileNotFoundException, IOException {
        boolean loop = true;
        File file = new File("E:\\SpringPracaDomowa\\all_data.txt");
        Scanner fileScanner = new Scanner(file);
        boolean exists = file.exists();
        if (!exists) {
            file.createNewFile();
        }
        while (fileScanner.hasNext()) {
            String str = fileScanner.nextLine();
            String[] actualValue = str.split(" ");
            gamerRepository.save(new Gamer(actualValue[0], Double.parseDouble(actualValue[1]), (actualValue[7]), actualValue[6]));
        }
        fileScanner.close();
    }

    @RequestMapping("/gamer/get/{id}")
    @ResponseBody
    public String getGamer(@PathVariable int id) {
        Optional<Gamer> gamer = gamerRepository.findById(id);
        return gamer.get().toString();
    }

    @GetMapping("/")
    public String showAllGamers(Model model) {
        Gamer[] gamers = gamerRepository.findAll().toArray(new Gamer[0]);
        model.addAttribute("gamers", gamers);
        model.addAttribute("servers", gamers[0].getAllServers());
        return "gamer/pickTeams";
    }


    @PostMapping("/")
    public String processForm(HttpServletRequest request, Model model) {
//        model.asMap().forEach((k, v) -> logger.debug(k + ": " + v));
        String steamsReady = request.getParameter("teamsReady");
        boolean teamsReady = "true".equals(steamsReady);
        String[] gamersIdString = request.getParameterValues("gamers");
        String server = request.getParameter("server");
        int[] gamersId = new int[10];
        Gamer[] gamers = new Gamer[10];
        for (int i = 0; i < gamersIdString.length; i++) {
            gamersId[i] = Integer.parseInt(gamersIdString[i]);
        }

        for (int i = 0; i < 10; i++) {
            gamers[i] = gamerRepository.findById(gamersId[i]).get();
            gamers[i].setMmr(gamers[i].getMmr() - gamers[i].serverHandicap(server));
        }
        if (teamsReady == true) {
            for (int i = 0; i < 5; i++) {
                team1gamers[i] = gamers[i];
                team2gamers[i] = gamers[i + 5];
            }
            checkTeam1(team1gamers, server);
            checkTeam2(team2gamers, server);
        } else {
            double perfectBalance = checkPerfectBalance(gamers);
            int spr5 = 0, d = 0;
            double licznik = 0, test = 0, best = 0, bestd = 1000;
            for (int i = 1023; i > 0; i--) {

                for (int k = 0; k < 10; ++k) {
                    if (((i >> k) & 1) == 1) {
                        licznik = licznik + gamers[k].getMmr();
                        spr5++;
                    }
                }
                test = abs(licznik - perfectBalance);
                if (test < bestd && spr5 == 5) {
                    best = licznik;
                    bestd = test;
                    for (int b = 0; b < 10; ++b) {
                        if (((i >> b) & 1) == 1) {
                            team1gamers[d].cloneValues(gamers[b]);
                            d++;
                        }
                    }
                }
                licznik = 0;
                spr5 = 0;
                d = 0;
            }
            System.out.println("team nr1 points - " + Math.round(best * 10) / 10f + "\n");
            checkTeam1(team1gamers, server);
            int test2 = 0, m = 0;
            double best2 = 0;
            for (int c = 0; c < 10; c++) {
                for (int d1 = 0; d1 < 5; d1++) {
                    if (gamers[c].getName() == team1gamers[d1].getName()) test2++;
                }
                if (test2 == 0) {
                    team2gamers[m].cloneValues(gamers[c]);
                    best2 = best2 + team2gamers[m].getMmr();
                    m++;
                }
                test2 = 0;
            }
            System.out.println("team nr2 points - " + Math.round(best2 * 10) / 10f + "\n");
            checkTeam2(team2gamers, server);
        }
        model.addAttribute("team1", team1gamers);
        model.addAttribute("team2", team2gamers);
        model.addAttribute("server", server);

        return "gamer/teamsScores";
    }

    @PostMapping("/updateScores")
    public String updateScores(HttpServletRequest request, Model model) {

        String server = request.getParameter("server");
        boolean suddenDeath = "true".equals(request.getParameter("suddendeath"));
        String suddenDeathWhoWon = request.getParameter("teamSDWinner");
        String[] sTeam1titans = request.getParameterValues("team1tytanId");
        String[] sTeam2titans = request.getParameterValues("team2tytanId");

        int[] team1gamersId = Arrays.stream(request.getParameterValues("team1gamersId")).mapToInt(Integer::parseInt).toArray();
        int[] team1elims = Arrays.stream(request.getParameterValues("team1eliminacjeId")).mapToInt(Integer::parseInt).toArray();
        int[] team1flags = Arrays.stream(request.getParameterValues("team1flagiId")).mapToInt(Integer::parseInt).toArray();
        int[] team2gamersId = Arrays.stream(request.getParameterValues("team2gamersId")).mapToInt(Integer::parseInt).toArray();
        int[] team2elims = Arrays.stream(request.getParameterValues("team2eliminacjeId")).mapToInt(Integer::parseInt).toArray();
        int[] team2flags = Arrays.stream(request.getParameterValues("team2flagiId")).mapToInt(Integer::parseInt).toArray();
        String mapPlayed = request.getParameter("map");

        int team1flagsTotal = 0, team2flagsTotal = 0, whoWon = 0, streak = 0, streak2 = 0;

        for (int i = 0; i < 5; i++) {
            team1flagsTotal += team1flags[i];
            team2flagsTotal += team2flags[i];
        }
        if (team1flagsTotal > team2flagsTotal) {
            whoWon = 1;
        } else if (team1flagsTotal < team2flagsTotal) {
            whoWon = 2;
        } else {
            if (suddenDeathWhoWon.equals("team1")) whoWon = 1;
            if (suddenDeathWhoWon.equals("team2")) whoWon = 2;
        }

        Match match = new Match();
        match.setMap(Match.Map_Name.valueOf(mapPlayed));
        match.setServer(server);
        matchDao.saveMatch(match);

        Team team1 = new Team();
        Team team2 = new Team();
        team1.setFlagAdvantage(team1flagsTotal - team2flagsTotal);
        team2.setFlagAdvantage(team2flagsTotal - team1flagsTotal);

        if (whoWon == 1) {
            team1.setWinOrLoose(1);
            team2.setWinOrLoose(0);
        }
        if (whoWon == 2) {
            team1.setWinOrLoose(0);
            team2.setWinOrLoose(1);
        }
        teamDao.saveTeam(team1);
        teamDao.saveTeam(team2);

        for (int i = 0; i < 5; i++) {
            team1gamers[i] = gamerRepository.findById(team1gamersId[i]).get();
            team2gamers[i] = gamerRepository.findById(team2gamersId[i]).get();

            int countDown = Integer.parseInt(team1gamers[i].getLastTen(), 2);
            int countDown2 = Integer.parseInt(team2gamers[i].getLastTen(), 2);

            for (int a = 0; a < 10; a++) {
                if ((countDown & 1) == 1) streak++;
                countDown = countDown >> 1;
                if ((countDown2 & 1) == 1) streak2++;
                countDown2 = countDown2 >> 1;
            }

            double points = 0, points2 = 0;
            if ((streak == 7 || streak == 8) && whoWon == 1) points = 1.2d;
            else if ((streak == 2 || streak == 3) && whoWon == 2) points = -1.2d;
            else if (streak > 1 && whoWon == 2) points = -1;
            else if (streak >= 9 && whoWon == 1) points = 1.5d;
            else if (streak <= 1 && whoWon == 2) points = -1.5d;
            else if (streak < 9 && whoWon == 1) points = 1;

            if ((streak2 == 7 || streak2 == 8) && whoWon == 2) points2 = 1.2d;
            else if ((streak2 == 2 || streak2 == 3) && whoWon == 1) points2 = -1.2d;
            else if (streak2 > 1 && whoWon == 1) points2 = -1;
            else if (streak2 >= 9 && whoWon == 2) points2 = 1.5d;
            else if (streak2 <= 1 && whoWon == 1) points2 = -1.5d;
            else if (streak2 < 9 && whoWon == 2) points2 = 1;


            if (whoWon == 1) {
                points = points + (team1.getFlagAdvantage() / 5.0d) - 0.2d;
                points2 = points2 + (team2.getFlagAdvantage() / 5.0d) + 0.2d;

                if (suddenDeath) {
                    points = 0.5d;
                    points2 = -0.5d;
                } else {
                    team1gamers[i].setLastTen(Integer.toBinaryString((Integer.parseInt(team1gamers[i].getLastTen(), 2) >> 1) | 512));
                    team2gamers[i].setLastTen(Integer.toBinaryString(Integer.parseInt(team2gamers[i].getLastTen(), 2) >> 1));
                }
            }
            if (whoWon == 2) {
                points2 = points2 + team2.getFlagAdvantage() / 5.0d - 0.2d;
                points = points + team1.getFlagAdvantage() / 5.0d + 0.2d;

                if (suddenDeath) {
                    points = -0.5d;
                    points2 = 0.5d;
                } else {
                    team1gamers[i].setLastTen(Integer.toBinaryString(Integer.parseInt(team1gamers[i].getLastTen(), 2) >> 1));
                    team2gamers[i].setLastTen(Integer.toBinaryString((Integer.parseInt(team2gamers[i].getLastTen(), 2) >> 1) | 512));
                }
            }
            team1gamers[i].setMmr(Math.round((team1gamers[i].getMmr() + points) * 10) / 10d);
            team2gamers[i].setMmr(Math.round((team2gamers[i].getMmr() + points2) * 10) / 10d);
            streak = 0;
            streak2 = 0;
        }
        for (int i = 0; i < 5; i++) {
            gamerRepository.save(team1gamers[i]);
            gamerRepository.save(team2gamers[i]);
            MatchGamer matchGamer1 = new MatchGamer();
            MatchGamer matchGamer2 = new MatchGamer();

            matchGamer1.setGamer(team1gamers[i]);
            matchGamer1.setMatch(match);
            matchGamer1.setTeam(team1);

            matchGamer2.setGamer(team2gamers[i]);
            matchGamer2.setMatch(match);
            matchGamer2.setTeam(team2);
            matchGamerDao.saveMatchGamer(matchGamer1);
            matchGamerDao.saveMatchGamer(matchGamer2);

            KillsAndCaps killsAndCaps1 = new KillsAndCaps();
            KillsAndCaps killsAndCaps2 = new KillsAndCaps();

            killsAndCaps1.setKills(team1elims[i]);
            killsAndCaps1.setCaps(team1flags[i]);
            killsAndCaps1.setTitan(KillsAndCaps.Titan_Name.valueOf(sTeam1titans[i]));
            killsAndCaps1.setMatchGamer(matchGamer1);

            killsAndCaps2.setKills(team2elims[i]);
            killsAndCaps2.setCaps(team2flags[i]);
            killsAndCaps2.setTitan(KillsAndCaps.Titan_Name.valueOf(sTeam2titans[i]));
            killsAndCaps2.setMatchGamer(matchGamer2);

            killsAndCapsDao.saveKillsAndCaps(killsAndCaps1);
            killsAndCapsDao.saveKillsAndCaps(killsAndCaps2);
        }
        model.addAttribute("team1", team1gamers);
        model.addAttribute("team2", team2gamers);
        model.addAttribute("server", server);
        return "gamer/teamsScores";


    }

    public void checkTeam1(Gamer[] team1, String server) {
        for (Gamer a : team1) {
            System.out.println(a.getName() + " - " + a.getMmr() + " - " + " - handicap -" + a.serverHandicap(server));
        }
        System.out.println("team1 checked \n");
    }

    public void checkTeam2(Gamer[] team2, String server) {
        for (Gamer a : team2) {
            System.out.println(a.getName() + " - " + a.getMmr() + " - " + " - handicap -" + a.serverHandicap(server));
        }
        System.out.println("team2 checked \n");
    }

    public double checkPerfectBalance(Gamer[] lobby) {
        double sum = 0;
        for (Gamer a : lobby) {
            sum = sum + a.getMmr();
        }
        double perfectBalance = sum / 2;
        System.out.println("perfect balance is - " + Math.round(perfectBalance * 10) / 10f + "\n");
        return perfectBalance;
    }
}
