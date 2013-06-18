// Generated by CoffeeScript 1.6.2
(function() {
  var Ball, Drawable, Engine, Field, Player, ball, engine, field, i, players, plr, _fn, _fn1, _i, _j, _k, _len,
    __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

  Engine = (function() {
    function Engine(ball) {
      var now;

      this.ball = ball;
      console.log(this.ball);
      this.resolution = [640, 480];
      this.bgcolor = 0..fffff0;
      this.obj_stack = [];
      this.scene = new THREE.Scene;
      this.camera = new THREE.PerspectiveCamera(75, this.resolution[0] / this.resolution[1], 1, 10000);
      this.camera.position.y = 33;
      this.camera.position.z = 100;
      this.camera_mode = "BIRD";
      this.amb_light = new THREE.AmbientLight(0xffffff, 1.0);
      this.scene.add(this.amb_light);
      this.renderer = new THREE.WebGLRenderer;
      this.renderer.clearColor = this.bgcolor;
      this.renderer.clear;
      this.renderer.setSize(this.resolution[0], this.resolution[1]);
      now = new Date;
      this.start_time = now.getTime();
      this.add(this.ball);
      this.mean_ball_cnt = 1000;
      this.mean_ball_pos = {
        x: 0,
        y: 0,
        z: 0
      };
    }

    Engine.prototype.get_canvas = function(target_div) {
      return this.renderer.domElement;
    };

    Engine.prototype.add = function(obj) {
      var drawable, _i, _len, _ref, _results;

      this.obj_stack.push(obj);
      _ref = obj.drawables;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        drawable = _ref[_i];
        _results.push(this.scene.add(drawable));
      }
      return _results;
    };

    Engine.prototype.render = function() {
      var obj, time, _i, _j, _len, _len1, _ref, _ref1;

      time = (new Date).getTime() - this.start_time;
      _ref = this.obj_stack;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        obj = _ref[_i];
        obj.follow(this.camera.position);
      }
      _ref1 = this.obj_stack;
      for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
        obj = _ref1[_j];
        obj.animate(time);
      }
      this.mean_ball_pos.x = this.mean_ball_cnt * this.mean_ball_pos.x + this.ball.ball.position.x;
      this.mean_ball_pos.x /= this.mean_ball_cnt + 1;
      this.mean_ball_pos.y = this.mean_ball_cnt * this.mean_ball_pos.y + this.ball.ball.position.y;
      this.mean_ball_pos.y /= this.mean_ball_cnt + 1;
      this.mean_ball_pos.z = this.mean_ball_cnt * this.mean_ball_pos.z + this.ball.ball.position.z;
      this.mean_ball_pos.z /= this.mean_ball_cnt + 1;
      console.log(this.mean_ball_pos);
      switch (this.camera_mode) {
        case "BIRD":
          this.camera.position.set(0, 60, 0);
          this.camera.rotation.set(-Math.PI / 2, 0, 0);
          break;
        case "KEEPERA":
          this.camera.position.set(-60, 10, 0);
          this.camera.lookAt(this.mean_ball_pos);
          break;
        case "KEEPERB":
          this.camera.position.set(60, 10, 0);
          this.camera.lookAt(this.mean_ball_pos);
          break;
        default:
          this.camera.position.x = 5 * Math.cos(time / 1200);
          this.camera.position.y = 33 + 5 * Math.sin(time / 600);
          this.camera.position.z = 50 + 25 * Math.sin(time / 2700);
          this.camera.lookAt(this.mean_ball_pos);
      }
      return this.renderer.render(this.scene, this.camera);
    };

    Engine.prototype.set_cam_mode = function(mode) {
      return this.camera_mode = mode;
    };

    return Engine;

  })();

  Drawable = (function() {
    function Drawable() {
      this.drawables = [];
      this.followers = [];
    }

    Drawable.prototype.animate = function(time) {};

    Drawable.prototype.follow = function(target) {
      var f, _i, _len, _ref, _results;

      _ref = this.followers;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        f = _ref[_i];
        _results.push((function(f) {
          return f.lookAt(target);
        })(f));
      }
      return _results;
    };

    Drawable.prototype.toString = function() {
      return "Drawable";
    };

    return Drawable;

  })();

  Field = (function(_super) {
    __extends(Field, _super);

    function Field() {
      var geometry, mat_cfg, material;

      this.width = 120;
      this.height = 90;
      geometry = new THREE.PlaneGeometry(this.width, this.height);
      mat_cfg = {
        map: new THREE.ImageUtils.loadTexture("img/Fussballfeld.svg"),
        side: THREE.DoubleSide
      };
      material = new THREE.MeshLambertMaterial(mat_cfg);
      this.field = new THREE.Mesh(geometry, material);
      this.field.rotation.x = Math.PI / 2;
      this.drawables = [this.field];
      this.followers = [];
    }

    Field.prototype.toString = function() {
      return "Field";
    };

    return Field;

  })(Drawable);

  Ball = (function(_super) {
    __extends(Ball, _super);

    function Ball() {
      var geometry, mat_cfg, material;

      geometry = new THREE.PlaneGeometry(2, 2);
      mat_cfg = {
        map: new THREE.ImageUtils.loadTexture("img/ball.png"),
        alphaTest: 0.5
      };
      material = new THREE.MeshBasicMaterial(mat_cfg);
      this.ball = new THREE.Mesh(geometry, material);
      geometry = new THREE.PlaneGeometry(1, 1);
      mat_cfg = {
        map: new THREE.ImageUtils.loadTexture("img/shadow.png"),
        transparent: true
      };
      material = new THREE.MeshBasicMaterial(mat_cfg);
      this.shadow = new THREE.Mesh(geometry, material);
      this.shadow.rotation.x = -Math.PI / 2;
      this.shadow.position.y = 0.01;
      this.followers = [this.ball];
      this.drawables = [this.ball, this.shadow];
      this.ball.position.set(0, 0, 0);
    }

    Ball.prototype.animate = function(time) {
      this.ball.position.y = 1;
      this.ball.position.y += Math.abs(2 * Math.sin(time / 120));
      this.ball.position.x = 2;
      this.ball.position.x += 2 * Math.cos(time / 120);
      this.shadow.position.x = this.ball.position.x;
      this.shadow.position.z = this.ball.position.z;
      this.shadow.scale.x = this.ball.position.y;
      this.shadow.scale.x -= this.ball.geometry.height;
      this.shadow.scale.y = this.ball.position.y;
      this.shadow.scale.y -= this.ball.geometry.height;
      this.shadow.scale.z = this.ball.position.y;
      return this.shadow.scale.z -= this.ball.geometry.height;
    };

    Ball.prototype.toString = function() {
      return "Ball";
    };

    return Ball;

  })(Drawable);

  Player = (function(_super) {
    __extends(Player, _super);

    function Player(tricot_image) {
      var geometry, mat_cfg, material;

      this.tricot_image = tricot_image;
      geometry = new THREE.PlaneGeometry(4, 4);
      mat_cfg = {
        map: new THREE.ImageUtils.loadTexture(this.tricot_image),
        alphaTest: 0.5
      };
      material = new THREE.MeshBasicMaterial(mat_cfg);
      this.shirt = new THREE.Mesh(geometry, material);
      this.shirt.position.y = this.shirt.geometry.height / 2;
      geometry = new THREE.PlaneGeometry(2, 2);
      mat_cfg = {
        map: new THREE.ImageUtils.loadTexture("img/shadow.png"),
        alpha: 0.5,
        transparent: true
      };
      material = new THREE.MeshBasicMaterial(mat_cfg);
      this.shadow = new THREE.Mesh(geometry, material);
      this.shadow.rotation.x = -Math.PI / 2;
      this.shadow.position.y = 0.01;
      this.followers = [this.shirt];
      this.drawables = [this.shirt, this.shadow];
      this.last_update = 0;
      this.target_pos = {
        x: 0,
        y: 0
      };
      this.anim_factor = 10;
    }

    Player.prototype.update = function(time, data) {
      this.target_pos = data.pos;
      return this.last_update = time;
    };

    Player.prototype.animate = function(time) {
      this.shirt.position.x = (this.anim_factor * this.shirt.position.x + this.target_pos.x) / (this.anim_factor + 1);
      this.shirt.position.z = (this.anim_factor * this.shirt.position.z + this.target_pos.y) / (this.anim_factor + 1);
      this.shadow.position.x = this.shirt.position.x;
      this.shadow.position.z = this.shirt.position.z;
      this.shadow.scale.x = this.shirt.position.y;
      this.shadow.scale.x -= this.shirt.geometry.height;
      this.shadow.scale.y = this.shirt.position.y;
      this.shadow.scale.y -= this.shirt.geometry.height;
      this.shadow.scale.z = this.shirt.position.y;
      return this.shadow.scale.z -= this.shirt.geometry.height;
    };

    Player.prototype.toString = function() {
      return "Player(" + this.tricot_image + ")";
    };

    return Player;

  })(Drawable);

  ball = new Ball();

  field = new Field();

  engine = new Engine(ball);

  players = [];

  _fn = function(i) {
    return players.push(new Player("img/trikot_gelb_" + i + ".svg"));
  };
  for (i = _i = 1; _i <= 11; i = ++_i) {
    _fn(i);
  }

  _fn1 = function(i) {
    return players.push(new Player("img/trikot_rot_" + i + ".svg"));
  };
  for (i = _j = 1; _j <= 11; i = ++_j) {
    _fn1(i);
  }

  engine.add(field);

  for (_k = 0, _len = players.length; _k < _len; _k++) {
    plr = players[_k];
    engine.add(plr);
  }

  $(function() {
    var button, run, update, _fn2, _l, _len1, _ref;

    console.log("Initializing");
    $("#field").append(engine.get_canvas());
    _ref = $("#perspectives_menu li button");
    _fn2 = function(button) {
      button.onclick = function() {
        return engine.set_cam_mode(button.id);
      };
      return console.log(button);
    };
    for (_l = 0, _len1 = _ref.length; _l < _len1; _l++) {
      button = _ref[_l];
      _fn2(button);
    }
    run = function() {
      requestAnimationFrame(run);
      return engine.render();
    };
    requestAnimationFrame(run);
    update = function() {
      var _len2, _m, _results;

      window.setTimeout(update, 1000);
      _results = [];
      for (_m = 0, _len2 = players.length; _m < _len2; _m++) {
        plr = players[_m];
        _results.push((function(plr) {
          var data;

          if (Math.random() > 0.5) {
            return;
          }
          data = {
            pos: {
              x: Math.random() * field.width - field.width / 2,
              y: Math.random() * field.height - field.height / 2
            }
          };
          return plr.update(0, data);
        })(plr));
      }
      return _results;
    };
    return window.setTimeout(update, 1000);
  });

}).call(this);
