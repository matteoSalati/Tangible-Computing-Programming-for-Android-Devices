const FOREVER = 10,
      IF_LEFT = 6,
      IF_RIGHT = 7,
      IF_FWD = 8,
      GO_FWD = 1,
      ELSE = 4,
      TURN_LEFT = 2,
      TURN_RIGHT = 3,
      END_IF = 9,
      END_WHILE = 5;

var img_file = null;

function createCameraButtons() {

  var runButton = document.querySelector('#runButton');
  if (runButton == null) return setTimeout(createCameraButtons, 250);

  var modal = document.createElement('div');
  modal.id = 'modal-frame';
  var modalClose = document.createElement('div');
  modalClose.appendChild(document.createTextNode('×'));
  modalClose.id = 'modal-close';
  modal.appendChild(modalClose);
  modalClose.onclick = function () {
      modal.style.display = "none";
  }
  var modalImage = document.createElement('img');
  modalImage.id = 'modal-content';
  modal.appendChild(modalImage);
  document.querySelector('body').appendChild(modal);

  var input = document.createElement('input');
  input.type = "file";
  input.accept = "image/*";
  input.acquire = "";
  input.id = 'camera-button';
  input.onchange = function ()
  {
    img_file = input.files[0];
    processImage(input.files[0]);
  };
  var img = document.createElement('img');
  img.src = 'camera.png';
  var label = document.createElement('label');
  label.appendChild(img);
  //label.appendChild(document.createTextNode('Camera'));
  label.appendChild(input);
  label.htmlFor = 'camera-button';
  label.id = 'camera-label';

  var image = document.createElement('img');
  image.id = 'captured-image';
  image.src = 'common/1x1.gif';
  image.onclick = function () {
    modal.style.display = "block";
    modalImage.src = image.src;
  }

  runButton.parentNode.parentNode.querySelector('td:first-child').appendChild(label);
  runButton.parentNode.parentNode.querySelector('td:first-child').appendChild(image);

  runButton.addEventListener('click', function(event) {
      var xml = Blockly.Xml.workspaceToDom(Blockly.getMainWorkspace());
	  console.log('running xml : ' + xml);
      var code = Blockly.Xml.domToText(xml);
      console.log('running: ' + code);

      var url = new URL(window.location.href);
      var player = url.searchParams.get("player");
      if (player) {
        // store player
      } else {
        // load player
      }
      var level = url.searchParams.get("level");

      var form = new FormData(),
      xhr = new XMLHttpRequest();
      form.append('player', player);
      form.append('match', level);
      form.append('camera_input', file);
      form.append('solution_xml', code);
      xhr.open('post', '192.168.1.1:6543/upload_solution', true);
      xhr.send(form);
  }, false);
}

//createCameraButtons();

function processImage(file) {
  var reader = new FileReader();

  reader.onload = function (e) {
    var img = new Image();

    img.onload = function () {
      var w = 1200, h = 1200;
      if (img.height >= img.width) {
        var w = Math.round(img.width * h / img.height);
      } else {
        var h = Math.round(img.height * w / img.width);
      }
      var c = document.createElement('canvas');
      c.width = w;
      c.height = h;
      var ctx = c.getContext('2d');
      ctx.drawImage(img, 0, 0, img.width, img.height, 0, 0, w, h);
      var imageData = ctx.getImageData(0, 0, w, h);

      var detector = new AR.Detector();
      var markers = detector.detect(imageData);
      markers.sort(compareMarkers);
      markers = removeDuplicates(markers);
      var instructions = [];
      for (k of markers)
		  {
        instructions.push(k.id);
        console.log("instrrrrrrrrrrrrrrrrrrrrrrrrrrr: " + k.id);
        ctx.fillStyle = 'Lime';
        ctx.fillText(k.id, k.corners[0].x, k.corners[0].y);
        ctx.fillStyle = 'Cyan';
        ctx.fillText(k.id, k.corners[1].x, k.corners[1].y);
        ctx.stroke();
      }
      document.querySelector('#captured-image').src = c.toDataURL();

      code = parseCode(instructions);
	  console.log(instructions);
      if (instructions.length > 0) code = 'error';
      console.log(code);
      code = '<xml xmlns="http://www.w3.org/1999/xhtml">' + code + '</xml>';
      Blockly.getMainWorkspace().clear();
      var xml = Blockly.Xml.textToDom(code);
      Blockly.Xml.domToWorkspace(xml, Blockly.getMainWorkspace());
    };

    img.src = e.target.result;
  };
  reader.readAsDataURL(file);
}

function removeDuplicates(markers) {
  var diagonal = 0;
  for (i of markers) {
    var dx = i.corners[0].x - i.corners[2].x, dy = i.corners[0].y - i.corners[2].y;
    diagonal += Math.sqrt(dx * dx + dy * dy);
    dx = i.corners[1].x - i.corners[3].x, dy = i.corners[1].y - i.corners[3].y;
    diagonal += Math.sqrt(dx * dx + dy * dy);
  }
  diagonal /= 2 * markers.length;

  var result = [];
  for (i of markers) {
    var found = false;
    for (j of result) {
      if (i !== j && i.id == j.id &&
          Math.abs(i.corners[0].x - j.corners[0].x) < diagonal &&
          Math.abs(i.corners[0].y - j.corners[0].y) < diagonal) {
        found = true;
      }
    }
    if (!found) result.push(i);
  }
  return result;
}

function compareMarkers(a, b) {
  var a0 = a.corners[0],
      a1 = a.corners[1],
      b0 = b.corners[0],
      result = 0;
  if (Math.abs(a0.y - a1.y) <= Math.abs(a0.x - a1.x)) {
    result = a0.y - b0.y;
    if (a0.x > a1.x) result *= -1;
  } else {
    result = a0.x - b0.x;
    if (a0.y < a1.y) result *= -1;
  }
  return Math.sign(result);
}

function parseCode(instructions) {
  if (instructions.length == 0) return '';
  var result = '';
  var i = instructions[0];
  if (i == IF_FWD || i == IF_LEFT || i == IF_RIGHT) {
    result = parseIf(instructions);
  } else if (i == FOREVER) {
    result = parseWhile(instructions);
  } else if (i == GO_FWD) {
    instructions.splice(0, 1);
    result = '<block type="maze_moveForward">';
  } else if (i == TURN_LEFT) {
    instructions.splice(0, 1);
    result = '<block type="maze_turn"><field name="DIR">turnLeft</field>';
  } else if (i == TURN_RIGHT) {
    instructions.splice(0, 1);
    result = '<block type="maze_turn"><field name="DIR">turnRight</field>';
  } else return '';
  var inner = parseCode(instructions);
  if (inner == 'error') return 'error';
  else if (inner == '') return result + '</block>';
  return result + '<next>' + inner + '</next></block>';
}

function parseIf(instructions) {
  var dir = 'isPathForward';
  if (instructions[0] == IF_LEFT) dir = 'isPathLeft';
  else if (instructions[0] == IF_RIGHT) dir = 'isPathRight';

  instructions.splice(0, 1);
  var inner1 = parseCode(instructions);
  if (inner1 == 'error') return 'error';
  if (instructions[0] == END_IF) {
    instructions.splice(0, 1);
    return '<block type="maze_if"><field name="DIR">' + dir + '</field>'
           + '<statement name="DO">' + inner1 + '</statement>';
  } else if (instructions[0] == ELSE) {
    instructions.splice(0, 1);
    var inner2 = parseCode(instructions);
    if (inner2 == 'error' || instructions[0] != END_IF) return 'error';
    instructions.splice(0, 1);
    return '<block type="maze_ifElse"><field name="DIR">' + dir + '</field>'
         + '<statement name="DO">' + inner1 + '</statement>'
         + '<statement name="ELSE">' + inner2 + '</statement>';
  }
  return 'error';
}

function parseWhile(instructions) {
  instructions.splice(0, 1);
  var inner = parseCode(instructions);
  if (inner == 'error' || instructions[0] != END_WHILE) return 'error';
  instructions.splice(0, 1);
  return '<block type="maze_forever"><statement name="DO">' + inner + '</statement>';
}
